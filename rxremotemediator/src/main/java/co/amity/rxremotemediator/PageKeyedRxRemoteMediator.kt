package co.amity.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import kotlin.math.ceil
import kotlin.math.max

@ExperimentalPagingApi
abstract class PageKeyedRxRemoteMediator<ENTITY : Any, TOKEN : AmityQueryToken>(val nonce: Int, val queryParameters: Map<String, Any> = mapOf(), val tokenDao: AmityQueryTokenDao) :
    AmityRxRemoteMediator<ENTITY>() {

    final override fun initializeSingle(): Single<InitializeAction> {
        return Single.just(InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    final override fun loadSingle(loadType: LoadType, state: PagingState<Int, ENTITY>): Single<MediatorResult> {
        val pageSize = state.config.pageSize
        return when (loadType) {
            LoadType.REFRESH -> {
                state.anchorPosition?.let { anchorPosition ->
                    val pageNumber = ceil(max(1, anchorPosition).toDouble() / state.config.pageSize.toDouble()).toInt()
                    tokenDao.getTokenByPageNumber(pageNumber = pageNumber, queryParameters = queryParameters, nonce = nonce)
                        .subscribeOn(Schedulers.io())
                        .flatMapSingle { fetchByToken(token = it) }
                        .map {
                            it.apply {
                                this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                this.pageNumber = pageNumber
                            }
                        }.flatMap {
                            insertToken(it, pageSize)
                                .andThen(Single.just<MediatorResult>(MediatorResult.Success(endOfPaginationReached = it.next == null)))
                        }.onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                } ?: run {
                    fetchFirstPage(pageSize = pageSize)
                        .subscribeOn(Schedulers.io())
                        .map {
                            it.apply {
                                this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                this.pageNumber = 1
                            }
                        }.flatMap {
                            insertToken(it, pageSize)
                                .andThen(Single.just<MediatorResult>(MediatorResult.Success(endOfPaginationReached = it.next == null)))
                        }.onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                }
            }
            LoadType.PREPEND -> {
                Single.just(MediatorResult.Success(endOfPaginationReached = true))
            }
            LoadType.APPEND -> {
                tokenDao.getLastQueryToken(queryParameters = queryParameters, nonce = nonce)
                    .subscribeOn(Schedulers.io())
                    .flatMapSingle<MediatorResult> { token ->
                        fetchByToken(token = token.next!!)
                            .map {
                                it.apply {
                                    this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                    this.pageNumber = token.pageNumber + 1
                                }
                            }
                            .flatMap {
                                insertToken(it, pageSize)
                                    .andThen(Single.just(MediatorResult.Success(endOfPaginationReached = it.next == null)))
                            }
                    }.onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
            }
        }
    }

    final override fun stackFromEnd(): Boolean {
        return false
    }

    abstract fun fetchFirstPage(pageSize: Int): Single<TOKEN>

    abstract fun fetchByToken(token: String): Single<TOKEN>

    private fun insertToken(token: TOKEN, pageSize: Int): Completable {
        return tokenDao.insertToken(token)
            .andThen(
                when (token.next == null) {
                    true -> tokenDao.deleteAfterPageNumber(
                        pageNumber = token.pageNumber,
                        nonce = nonce,
                        queryParameters = queryParameters
                    ).andThen(
                        tokenDao.deleteAfterPosition(
                            position = ((token.pageNumber - 1) * pageSize) + token.primaryKeys.size,
                            nonce = nonce,
                            queryParameters = queryParameters
                        )
                    )
                    false -> Completable.complete()
                }
            )
            .andThen(tokenDao.insertPagingIds(token.primaryKeys.mapIndexed { index, id ->
                AmityPagingId(queryParameters = queryParameters, id = id)
                    .apply {
                        this.nonce = this@PageKeyedRxRemoteMediator.nonce
                        this.position = ((token.pageNumber - 1) * pageSize) + index + 1
                    }
            }))
    }
}