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

    var generatedPosition = Integer.MAX_VALUE

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
                state.anchorPosition?.let { anchorPosition ->
                    val pageNumber = ceil(
                        max(
                            1,
                            anchorPosition
                        ).toDouble() / state.config.pageSize.toDouble()
                    ).toInt()
                    tokenDao.getTokenByPageNumber(
                        pageNumber = pageNumber,
                        queryParameters = queryParameters,
                        nonce = nonce
                    )
                        .subscribeOn(Schedulers.io())
                        .flatMapSingle { fetchByToken(token = it) }
                        .map {
                            it.apply {
                                this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                this.pageNumber = pageNumber
                            }
                        }.flatMap {
                            insertToken(it, pageSize)
                                .andThen(
                                    Single.just<MediatorResult>(
                                        MediatorResult.Success(
                                            endOfPaginationReached = it.next == null
                                        )
                                    )
                                )
                        }.onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                } ?: run {
                    fetchFirstPage(pageSize = pageSize)
                        .flatMap {
                            if (forceRefresh()) {
                                tokenDao.clearPagingIds(queryParameters, nonce)
                                    .andThen(Completable.defer {
                                        tokenDao.clearQueryToken(
                                            queryParameters,
                                            nonce
                                        )
                                    })
                                    .andThen(Single.defer { Single.just(it) })
                            } else {
                                Single.just(it)
                            }
                        }
                        .subscribeOn(Schedulers.io())
                        .map {
                            it.apply {
                                this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                this.pageNumber = 1
                            }
                        }.flatMap {
                            insertToken(it, pageSize)
                                .andThen(
                                    Single.just<MediatorResult>(
                                        MediatorResult.Success(
                                            endOfPaginationReached = if(stackFromEnd()) it.previous == null else it.next == null
                                        )
                                    )
                                )
                        }.onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                }
            }
            LoadType.PREPEND -> {
                if(stackFromEnd()) {
                    tokenDao.getLastQueryToken(queryParameters = queryParameters, nonce = nonce)
                        .subscribeOn(Schedulers.io())
                        .flatMapSingle<MediatorResult> { token ->
                            fetchByToken(token = token.previous!!)
                                .map {
                                    it.apply {
                                        this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                        this.pageNumber = token.pageNumber + 1
                                    }
                                }
                                .flatMap {
                                    insertToken(it, pageSize)
                                        .andThen(
                                            Single.just(
                                                MediatorResult.Success(
                                                    endOfPaginationReached = it.previous == null
                                                )
                                            )
                                        )
                                }
                        }.onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                } else {
                    Single.just(MediatorResult.Success(endOfPaginationReached = true))
                }
            }
            LoadType.APPEND -> {
                if(stackFromEnd()) {
                    Single.just(MediatorResult.Success(endOfPaginationReached = true))
                } else {
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
                                        .andThen(
                                            Single.just(
                                                MediatorResult.Success(
                                                    endOfPaginationReached = it.next == null
                                                )
                                            )
                                        )
                                }
                        }.onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                }
            }
        }
    }

    override fun stackFromEnd(): Boolean {
        return false
    }

    open fun forceRefresh(): Boolean = false

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

    fun insertPagingIds(id: String) {
        tokenDao.insertPagingIdsIfNeeded(
            AmityPagingId(queryParameters = queryParameters, id = id)
                .apply {
                    this@PageKeyedRxRemoteMediator.generatedPosition--
                    this.nonce = this@PageKeyedRxRemoteMediator.nonce
                    this.position = this@PageKeyedRxRemoteMediator.generatedPosition
                }
        ).subscribeOn(Schedulers.single())
            .subscribe()
    }
}