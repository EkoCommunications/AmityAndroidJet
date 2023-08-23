package co.amity.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.math.ceil
import kotlin.math.max

@OptIn(ExperimentalPagingApi::class)
abstract class PageKeyedRxRemoteMediator<ENTITY : Any, TOKEN : AmityQueryToken>(
    val nonce: Int,
    val queryParameters: Map<String, Any> = mapOf(),
    val tokenDao: AmityQueryTokenDao
) :
    AmityRxRemoteMediator<ENTITY>() {

    //var generatedPosition = Integer.MAX_VALUE
    var highestPosition = 0
    var lowestPosition = 0
    var prevToken: String? = null
    var nextToken: String? = null

    final override fun initializeSingle(): Single<InitializeAction> {
        return Single.just(InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    final override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, ENTITY>
    ): Single<MediatorResult> {
        val pageSize = state.config.pageSize
        return when (loadType) {
            LoadType.REFRESH -> {
                fetchFirstPage(pageSize = pageSize)
                    .flatMap {
                        tokenDao.clearPagingIds(queryParameters, nonce)
                            .andThen(Completable.defer {
                                tokenDao.clearQueryToken(
                                    queryParameters,
                                    nonce
                                )
                            })
                            .andThen(Single.defer { Single.just(it) })
                    }
                    .subscribeOn(Schedulers.io())
                    .map {
                        it.apply {
                            this.nonce = this@PageKeyedRxRemoteMediator.nonce
                            this.pageNumber = 1
                        }
                    }.flatMap {
                        insertToken(it, loadType)
                            .andThen(
                                Single.just<MediatorResult>(
                                    MediatorResult.Success(
                                        endOfPaginationReached = false
                                    )
                                )
                            )
                    }.onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
            }

            LoadType.PREPEND -> {
                if (prevToken != null) {
                    fetchByToken(token = prevToken!!)
                        .flatMap {
                            insertToken(it, loadType)
                                .andThen(
                                    Single.just(
                                        MediatorResult.Success(endOfPaginationReached = it.previous == null) as MediatorResult
                                    )
                                )
                        }
                        .onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                } else {
                    Single.just(MediatorResult.Success(endOfPaginationReached = true))
                }
            }

            LoadType.APPEND -> {
                if (nextToken == null) {
                    Single.just(MediatorResult.Success(endOfPaginationReached = true))
                } else {
                    fetchByToken(token = nextToken!!)
                        .flatMap {
                            insertToken(it, loadType)
                                .andThen(
                                    Single.just(
                                        MediatorResult.Success(endOfPaginationReached = it.next == null) as MediatorResult
                                    )
                                )
                        }
                        .onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                }
            }
        }
    }

    open fun forceRefresh(): Boolean = true

    abstract fun fetchFirstPage(pageSize: Int): Single<TOKEN>

    abstract fun fetchByToken(token: String): Single<TOKEN>

    private fun insertToken(token: TOKEN, loadType: LoadType): Completable {
        return Completable.fromAction {
            when (loadType) {
                LoadType.REFRESH -> {
                    prevToken = token.previous
                    nextToken = token.next
                }
                LoadType.PREPEND -> {
                    prevToken = token.previous
                }
                LoadType.APPEND -> {
                    nextToken = token.next
                }
            }
        }.andThen(tokenDao.insertPagingIds(token.primaryKeys.mapIndexed { index, id ->
                AmityPagingId(queryParameters = queryParameters, id = id)
                    .apply {
                        this.nonce = this@PageKeyedRxRemoteMediator.nonce
                        this.position = if(loadType == LoadType.PREPEND) { --lowestPosition } else { ++highestPosition }
                    }
            }))
    }

    fun insertPagingIds(id: String, isAppending: Boolean = true) {
        tokenDao.insertPagingIdsIfNeeded(
            AmityPagingId(queryParameters = queryParameters, id = id)
                .apply {
                    this.nonce = this@PageKeyedRxRemoteMediator.nonce
                    this.position = if(isAppending) { ++highestPosition } else { --lowestPosition }
                }
        ).subscribeOn(Schedulers.single())
            .subscribe()
    }
}