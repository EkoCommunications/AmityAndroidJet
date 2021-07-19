package co.amity.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import io.reactivex.Maybe
import io.reactivex.MaybeTransformer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.math.ceil
import kotlin.math.max

const val INVALID_PAGE_NUMBER = -1

@ExperimentalPagingApi
abstract class AmityRxRemoteMediator<ENTITY : Any, TOKEN : AmityQueryToken, TOKEN_DAO : AmityPagingTokenDao<TOKEN>>(private val tokenDao: TOKEN_DAO) :
    RxRemoteMediator<Int, ENTITY>() {

    final override fun loadSingle(loadType: LoadType, state: PagingState<Int, ENTITY>): Single<MediatorResult> {
        val pageSize = state.config.pageSize
        return when (loadType) {
            LoadType.REFRESH -> {
                state.anchorPosition?.let { anchorPosition ->
                    val pageNumber = ceil(max(1, anchorPosition).toDouble() / state.config.pageSize.toDouble()).toInt()
                    tokenDao.getTokenByPageNumber(pageNumber = pageNumber, primaryKeys = queryParameters())
                        .subscribeOn(Schedulers.io())
                        .flatMap { fetch(token = it, pageSize = pageSize) }
                        .flatMap { insertToken(applyQueryParametersToToken(it)) }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                } ?: run {
                    fetchFirstPage(pageSize = pageSize)
                        .subscribeOn(Schedulers.io())
                        .flatMap { insertToken(applyQueryParametersToToken(it)) }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                }
            }
            LoadType.PREPEND -> {
                if (stackFromEnd()) {
                    tokenDao.getFirstQueryToken(primaryKeys = queryParameters())
                        .subscribeOn(Schedulers.io())
                        .flatMap { fetch(token = it, pageSize = pageSize) }
                        .flatMap { insertToken(applyQueryParametersToToken(it)) }
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
                        .flatMap { fetch(token = it, pageSize = pageSize) }
                        .flatMap { insertToken(applyQueryParametersToToken(it)) }
                        .compose(interceptErrorAndEmpty)
                        .toSingle()
                }
            }
        }
    }

    abstract fun fetchFirstPage(pageSize: Int): Maybe<TOKEN>

    abstract fun fetch(token: String, pageSize: Int): Maybe<TOKEN>

    private fun insertToken(token: TOKEN): Maybe<MediatorResult> {
        return if (token.pageNumber == INVALID_PAGE_NUMBER) {
            Maybe.error<MediatorResult>(Exception("Invalid page number! please make sure that you specify a correct page number for a token"))
        } else {
            tokenDao.insertToken(token)
                .andThen(Maybe.just<MediatorResult>(MediatorResult.Success(if (stackFromEnd()) token.previous == null else token.next == null)))
        }
    }

    private val interceptErrorAndEmpty = MaybeTransformer<MediatorResult, MediatorResult> { upstream ->
        upstream.onErrorReturn { MediatorResult.Error(it) }
            .switchIfEmpty(Maybe.just(MediatorResult.Success(true)))
    }

    abstract fun queryParameters(): Map<String, Any>

    abstract fun applyQueryParametersToToken(token: TOKEN): TOKEN

    abstract fun stackFromEnd(): Boolean
}