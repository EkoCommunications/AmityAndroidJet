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

const val INVALID_PAGE_NUMBER = -1

@ExperimentalPagingApi
abstract class PageKeyedRxRemoteMediator<ENTITY : Any, TOKEN : AmityQueryToken, TOKEN_DAO : AmityQueryTokenDao<TOKEN>>(private val tokenDao: TOKEN_DAO) :
    AmityRxRemoteMediator<ENTITY>() {

    final override fun loadSingle(loadType: LoadType, state: PagingState<Int, ENTITY>): Single<MediatorResult> {
        val pageSize = state.config.pageSize
        return when (loadType) {
            LoadType.REFRESH -> {
                state.anchorPosition?.let { anchorPosition ->
                    val pageNumber = ceil(max(1, anchorPosition).toDouble() / state.config.pageSize.toDouble()).toInt()
                    tokenDao.getTokenByPageNumber(pageNumber = pageNumber, primaryKeys = queryParameters())
                        .subscribeOn(Schedulers.io())
                        .flatMap { fetch(token = it) }
                        .flatMap { insertToken(applyQueryParametersToToken(it).apply { this.pageNumber = pageNumber }) }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                } ?: run {
                    fetchFirstPage(pageSize = pageSize)
                        .subscribeOn(Schedulers.io())
                        .flatMap { insertToken(applyQueryParametersToToken(it).apply { this.pageNumber = 1 }) }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                }
            }
            LoadType.PREPEND -> {
                if (stackFromEnd()) {
                    tokenDao.getFirstQueryToken(primaryKeys = queryParameters())
                        .subscribeOn(Schedulers.io())
                        .flatMap {
                            fetch(token = it.previous!!)
                                .flatMap { newTokens -> insertToken(applyQueryParametersToToken(newTokens).apply { this.pageNumber = it.pageNumber + 1 }) }
                        }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                } else {
                    Single.just<MediatorResult>(MediatorResult.Success(false))
                }
            }
            LoadType.APPEND -> {
                if (stackFromEnd()) {
                    Single.just<MediatorResult>(MediatorResult.Success(false))
                } else {
                    tokenDao.getLastQueryToken(primaryKeys = queryParameters())
                        .subscribeOn(Schedulers.io())
                        .flatMap {
                            fetch(token = it.next!!)
                                .flatMap { newTokens -> insertToken(applyQueryParametersToToken(newTokens).apply { this.pageNumber = it.pageNumber + 1 }) }
                        }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                }
            }
        }
    }

    abstract fun fetchFirstPage(pageSize: Int): Maybe<TOKEN>

    abstract fun fetch(token: String): Maybe<TOKEN>

    private fun insertToken(token: TOKEN): Maybe<MediatorResult> {
        val isLastPage = when (stackFromEnd()) {
            true -> token.previous == null
            false -> token.next == null
        }
        return tokenDao.insertToken(token)
            .andThen(
                when (isLastPage) {
                    true -> deleteTokensAfterPageNumber(pageNumber = token.pageNumber)
                    false -> Completable.complete()
                }
            )
            .andThen(Maybe.just<MediatorResult>(MediatorResult.Success(isLastPage)))
    }

    private fun deleteTokensAfterPageNumber(pageNumber: Int): Completable {
        return tokenDao.deleteTokensAfterPageNumber(queryParameters = queryParameters(), pageNumber = pageNumber)
    }

    private val interceptErrorAndEmpty = MaybeTransformer<MediatorResult, MediatorResult> { upstream ->
        upstream.onErrorReturn { MediatorResult.Error(it) }
            .switchIfEmpty(Maybe.just(MediatorResult.Success(true)))
    }

    abstract fun applyQueryParametersToToken(token: TOKEN): TOKEN
}