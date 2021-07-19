package co.amity.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function
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
                        .flatMap(insertParams(skip))
                } ?: run {
                    fetch(skip = 0, limit = pageSize)
                        .flatMap(insertParams(0))
                }
            }
            LoadType.PREPEND -> Single.just(MediatorResult.Success(true))
            LoadType.APPEND -> {
                val skip = (++maxPageNumber - 1) * pageSize
                fetch(skip = skip, limit = pageSize)
                    .flatMap(insertParams(skip))
            }
        }
    }

    abstract fun fetch(skip: Int, limit: Int): Single<Array<PARAMS>>

    private fun insertParams(skip: Int): Function<Array<PARAMS>, Single<MediatorResult>> {
        return Function {
            val endOfPaginationReached = it.lastOrNull()?.endOfPaginationReached ?: true
            paramsDao.insertParams(it)
                .run {
                    when {
                        endOfPaginationReached && it.isEmpty() -> andThen(deleteParamsAfterIndex(skip))
                        endOfPaginationReached && it.isNotEmpty() -> andThen(deleteParamsAfterIndex(it.last().position))
                        else -> this
                    }
                }
                .andThen(Single.just<MediatorResult>(MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)))
        }
    }

    private fun deleteParamsAfterIndex(index: Int): Completable {
        return paramsDao.deleteParamsAfterIndex(queryParameters = queryParameters(), index = index)
    }

    final override fun stackFromEnd(): Boolean {
        return false
    }
}