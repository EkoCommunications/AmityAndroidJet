package co.amity.rxremotemediator

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

@ExperimentalPagingApi
abstract class ItemKeyedRxRemoteMediator<ENTITY : Any>(
    val nonce: Int,
    val queryParameters: Map<String, Any> = mapOf(),
    val tokenDao: AmityQueryTokenDao
) :
    AmityRxRemoteMediator<ENTITY>() {

    var generatedPosition = Integer.MAX_VALUE

    private var currentCursorId: String? = null
    private var currentPage = 0
    private var maxAnchor = 0


    final override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, ENTITY>
    ): Single<MediatorResult> {
        val pageSize = state.config.pageSize
        return when (loadType) {
            LoadType.REFRESH -> {
                Log.e(TAG, "anchorPosition : ${state.anchorPosition ?: 0}")
                Log.e(TAG, "maxAnchor : $maxAnchor")
                //check if the position already loaded
                if ((state.anchorPosition ?: 1) <= maxAnchor && currentCursorId != null) {
                    Log.e(TAG, "already loaded")
                    Single.just(MediatorResult.Success(endOfPaginationReached = true))
                }
                //check if it's not a first page, then fetch by cursorId
                else if ((state.anchorPosition ?: 1) >= maxAnchor && currentCursorId != null) {
                    maxAnchor = maxAnchor.coerceAtLeast(state.anchorPosition ?: 0)
                    Log.e(TAG, "load by cursorId : $currentCursorId")
                    fetchByCursor(cursorId = currentCursorId ?: "")
                        .doOnSuccess {
                            currentCursorId = it.lastCursorId
                        }
                        .flatMap(insertTokenWithResult(pageSize = pageSize))
                        .subscribeOn(Schedulers.io())
                        .onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                }
                //else it's the first page
                else if (currentCursorId == null) {
                    Log.e(TAG, "fetchFirstPage")
                    maxAnchor = maxAnchor.coerceAtLeast(state.anchorPosition ?: 0)
                    fetchFirstPage(pageSize = pageSize)
                        .doOnSuccess {
                            currentCursorId = it.lastCursorId
                        }
                        .flatMap {
                            currentPage = 0
                            if (forceRefresh()) {
                                tokenDao.clearPagingIds(queryParameters, nonce)
                                    .andThen(Single.defer { Single.just(it) })
                            } else {
                                Single.just(it)
                            }
                        }
                        .subscribeOn(Schedulers.io())
                        .flatMap(insertTokenWithResult(pageSize = pageSize))
                        .onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
                } else {
                    Log.e(TAG, "refresh else")
                    Single.just(MediatorResult.Success(endOfPaginationReached = true))
                }
            }
            LoadType.PREPEND -> {
                Log.e(TAG, "PREPEND")
                Single.just(MediatorResult.Success(endOfPaginationReached = true))
            }
            LoadType.APPEND -> {
                Log.e(TAG, "APPEND")
                maxAnchor = maxAnchor.coerceAtLeast(state.anchorPosition ?: 0)
                fetchByCursor(cursorId = currentCursorId ?: "")
                    .doOnSuccess {
                        currentCursorId = it.lastCursorId
                    }
                    .flatMap(insertTokenWithResult(pageSize = pageSize))
                    .subscribeOn(Schedulers.io())
                    .onErrorResumeNext { Single.just(MediatorResult.Error(it)) }
            }
        }
    }

    final override fun stackFromEnd(): Boolean {
        return false
    }

    open fun forceRefresh(): Boolean = false

    abstract fun fetchFirstPage(pageSize: Int): Single<PagingCursor>

    abstract fun fetchByCursor(cursorId: String): Single<PagingCursor>

    private fun insertTokenWithResult(pageSize: Int): Function<PagingCursor, Single<RemoteMediator.MediatorResult>> {
        return Function { cursor ->
            insertToken(cursor, pageSize)
                .andThen(
                    Single.just<MediatorResult>(
                        MediatorResult.Success(
                            endOfPaginationReached = cursor.primaryKeys.isEmpty()
                        )
                    )
                )
                .doOnSuccess {
                    currentPage++
                    Log.e(TAG, "after endOfPaginationReached : ${cursor.primaryKeys.isEmpty()}")
                    Log.e(TAG, "after loaded current page : $currentPage")
                    Log.e(TAG, "after loaded current cursorId : $currentCursorId")
                }
        }
    }

    private fun insertToken(cursorDto: PagingCursor, pageSize: Int): Completable {
        return tokenDao.insertPagingIds(cursorDto.primaryKeys.mapIndexed { index, id ->
            AmityPagingId(queryParameters = queryParameters, id = id)
                .apply {
                    this.nonce = this@ItemKeyedRxRemoteMediator.nonce
                    this.position = (currentPage * pageSize) + index + 1
                }
        })
    }

    fun insertPagingIds(id: String) {
        tokenDao.insertPagingIdsIfNeeded(
            AmityPagingId(queryParameters = queryParameters, id = id)
                .apply {
                    this@ItemKeyedRxRemoteMediator.generatedPosition--
                    this.nonce = this@ItemKeyedRxRemoteMediator.nonce
                    this.position = this@ItemKeyedRxRemoteMediator.generatedPosition
                }
        ).subscribeOn(Schedulers.single())
            .subscribe()
    }
}

private const val TAG = "IKRRM"