package co.amity.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.MaybeTransformer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.math.ceil
import kotlin.math.max

@ExperimentalPagingApi
abstract class PageKeyedRxRemoteMediator<ENTITY : Any, TOKEN : AmityQueryToken>(val nonce: Int, val queryParameters: Map<String, Any> = mapOf(), val tokenDao: AmityQueryTokenDao) :
    AmityRxRemoteMediator<ENTITY>() {

    final override fun loadSingle(loadType: LoadType, state: PagingState<Int, ENTITY>): Single<MediatorResult> {
        val pageSize = state.config.pageSize
        return when (loadType) {
            LoadType.REFRESH -> {
                state.anchorPosition?.let { anchorPosition ->
                    val pageNumber = ceil(max(1, anchorPosition).toDouble() / state.config.pageSize.toDouble()).toInt()
                    tokenDao.getTokenByPageNumber(pageNumber = pageNumber, queryParameters = queryParameters, nonce = nonce)
                        .subscribeOn(Schedulers.io())
                        .flatMap { fetch(token = it) }
                        .map {
                            it.apply {
                                this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                this.pageNumber = pageNumber
                            }
                        }
                        .flatMap { insertToken(it, pageSize) }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                } ?: run {
                    fetchFirstPage(pageSize = pageSize)
                        .subscribeOn(Schedulers.io())
                        .map {
                            it.apply {
                                this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                this.pageNumber = 1
                            }
                        }
                        .flatMap { insertToken(it, pageSize) }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                }
            }
            LoadType.PREPEND -> {
                if (stackFromEnd()) {
                    tokenDao.getFirstQueryToken(queryParameters = queryParameters, nonce = nonce)
                        .subscribeOn(Schedulers.io())
                        .flatMap { token ->
                            fetch(token = token.previous!!)
                                .map {
                                    it.apply {
                                        this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                        this.pageNumber = token.pageNumber + 1
                                    }
                                }
                                .flatMap { insertToken(it, pageSize) }
                        }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                } else {
                    Single.just<MediatorResult>(MediatorResult.Success(true))
                }
            }
            LoadType.APPEND -> {
                if (stackFromEnd()) {
                    Single.just<MediatorResult>(MediatorResult.Success(true))
                } else {
                    tokenDao.getLastQueryToken(queryParameters = queryParameters, nonce = nonce)
                        .subscribeOn(Schedulers.io())
                        .flatMap { token ->
                            fetch(token = token.next!!)
                                .map {
                                    it.apply {
                                        this.nonce = this@PageKeyedRxRemoteMediator.nonce
                                        this.pageNumber = token.pageNumber + 1
                                    }
                                }
                                .flatMap { insertToken(it, pageSize) }
                        }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                }
            }
        }
    }

    abstract fun fetchFirstPage(pageSize: Int): Maybe<TOKEN>

    abstract fun fetch(token: String): Maybe<TOKEN>

    private fun insertToken(token: TOKEN, pageSize: Int): Maybe<MediatorResult> {
        val isLastPage = when (stackFromEnd()) {
            true -> token.previous == null
            false -> token.next == null
        }
        return tokenDao.insertToken(token)
            .andThen(
                when (isLastPage) {
                    true -> tokenDao.deleteAfterPageNumber(
                        pageNumber = token.pageNumber,
                        nonce = nonce,
                        queryParameters = queryParameters
                    )
                    false -> Completable.complete()
                }
            )
            .andThen(tokenDao.insertPagingIds(token.uniqueIds.mapIndexed { index, id ->
                AmityPagingId(uniqueId = id, queryParameters = queryParameters)
                    .apply {
                        this.nonce = this@PageKeyedRxRemoteMediator.nonce
                        this.position = ((token.pageNumber - 1) * pageSize) + index + 1
                    }
            }))
            .andThen(Maybe.just<MediatorResult>(MediatorResult.Success(isLastPage)))
    }

    private val interceptErrorAndEmpty = MaybeTransformer<MediatorResult, MediatorResult> { upstream ->
        upstream.onErrorReturn { MediatorResult.Error(it) }
            .switchIfEmpty(Maybe.just(MediatorResult.Success(true)))
    }
}