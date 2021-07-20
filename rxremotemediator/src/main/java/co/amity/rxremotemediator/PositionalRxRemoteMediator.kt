package co.amity.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import io.reactivex.Completable
import io.reactivex.Single
import kotlin.math.ceil
import kotlin.math.max

private const val DEFAULT_MAX_PAGE_NUMBER = 0

@ExperimentalPagingApi
abstract class PositionalRemoteMediator<ENTITY : Any, PARAMS : AmityQueryParams>(
    private val nonce: Int,
    val queryParameters: Map<String, Any> = mapOf(),
    val paramsDao: AmityQueryParamsDao
) : AmityRxRemoteMediator<ENTITY>() {

    private var maxPageNumber = DEFAULT_MAX_PAGE_NUMBER

    final override fun loadSingle(loadType: LoadType, state: PagingState<Int, ENTITY>): Single<MediatorResult> {
        val pageSize = state.config.pageSize
        return when (loadType) {
            LoadType.REFRESH -> {
                state.anchorPosition?.let { anchorPosition ->
                    val pageNumber = ceil(max(1, anchorPosition).toDouble() / state.config.pageSize.toDouble()).toInt()
                    val skip = (pageNumber - 1) * pageSize
                    fetch(skip = skip, limit = pageSize)
                        .map {
                            it.apply {
                                this.hash = this@PositionalRemoteMediator.queryParameters.hashCode()
                                this.nonce = this@PositionalRemoteMediator.nonce
                                this.pageNumber = pageNumber
                            }
                        }
                        .flatMap { insertParams(it) }
                } ?: run {
                    fetch(skip = 0, limit = pageSize)
                        .map {
                            it.apply {
                                this.hash = this@PositionalRemoteMediator.queryParameters.hashCode()
                                this.nonce = this@PositionalRemoteMediator.nonce
                                this.pageNumber = 1
                            }
                        }
                        .flatMap { insertParams(it) }
                }
            }
            LoadType.PREPEND -> Single.just(MediatorResult.Success(true))
            LoadType.APPEND -> {
                val skip = (++maxPageNumber - 1) * pageSize
                fetch(skip = skip, limit = pageSize)
                    .map {
                        it.apply {
                            this.hash = this@PositionalRemoteMediator.queryParameters.hashCode()
                            this.nonce = this@PositionalRemoteMediator.nonce
                            this.pageNumber = maxPageNumber
                        }
                    }
                    .flatMap { insertParams(it) }
            }
        }
    }

    abstract fun fetch(skip: Int, limit: Int): Single<PARAMS>

    private fun insertParams(params: PARAMS): Single<MediatorResult> {
        return paramsDao.insertParams(params)
            .andThen(
                when (params.endOfPaginationReached) {
                    true -> paramsDao.deleteAfterPageNumber(pageNumber = params.pageNumber, hash = params.hash, nonce = nonce)
                    false -> Completable.complete()
                }
            )
            .andThen(Single.just<MediatorResult>(MediatorResult.Success(endOfPaginationReached = params.endOfPaginationReached)))
    }

    final override fun stackFromEnd(): Boolean {
        return false
    }
}