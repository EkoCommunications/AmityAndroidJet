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
abstract class PositionalRemoteMediator<ENTITY : Any, PARAMS : AmityQueryParams, PARAMS_DAO : AmityQueryParamsDao<PARAMS>>(private val paramsDao: PARAMS_DAO) :
    AmityRxRemoteMediator<ENTITY>() {

    private var maxPageNumber = DEFAULT_MAX_PAGE_NUMBER

    final override fun loadSingle(loadType: LoadType, state: PagingState<Int, ENTITY>): Single<MediatorResult> {
        val pageSize = state.config.pageSize
        return when (loadType) {
            LoadType.REFRESH -> {
                state.anchorPosition?.let { anchorPosition ->
                    val pageNumber = ceil(max(1, anchorPosition).toDouble() / state.config.pageSize.toDouble()).toInt()
                    val skip = (pageNumber - 1) * pageSize
                    fetch(skip = skip, limit = pageSize)
                        .flatMap { insertParams(it.apply { this.endOfPaginationReached = it.ids.size < pageSize }) }
                } ?: run {
                    fetch(skip = 0, limit = pageSize)
                        .flatMap { insertParams(it.apply { this.endOfPaginationReached = it.ids.size < pageSize }) }
                }
            }
            LoadType.PREPEND -> Single.just(MediatorResult.Success(true))
            LoadType.APPEND -> {
                val skip = (++maxPageNumber - 1) * pageSize
                fetch(skip = skip, limit = pageSize)
                    .flatMap { insertParams(it.apply { this.endOfPaginationReached = it.ids.size < pageSize }) }
            }
        }
    }

    abstract fun fetch(skip: Int, limit: Int): Single<PARAMS>

    private fun insertParams(params: PARAMS): Single<MediatorResult> {
        return paramsDao.insertParams(params)
            .andThen(
                when (params.endOfPaginationReached) {
                    true -> deleteTokensAfterPageNumber(pageNumber = params.pageNumber)
                    false -> Completable.complete()
                }
            )
            .andThen(Single.just<MediatorResult>(MediatorResult.Success(endOfPaginationReached = params.endOfPaginationReached)))
    }

    private fun deleteTokensAfterPageNumber(pageNumber: Int): Completable {
        return paramsDao.deleteTokensAfterPageNumber(queryParameters = queryParameters(), pageNumber = pageNumber)
    }

    final override fun stackFromEnd(): Boolean {
        return false
    }
}