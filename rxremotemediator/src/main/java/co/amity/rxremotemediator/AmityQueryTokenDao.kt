package co.amity.rxremotemediator

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface AmityQueryTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToken(token: AmityQueryToken): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPagingIds(pagingIds: List<AmityPagingId>): Completable

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPagingIdsIgnoreStrategy(pagingIds: List<AmityPagingId>): Completable

    fun insertPagingIdsIfNeeded(pagingId: AmityPagingId): Completable {
        return insertPagingIdsIgnoreStrategy(listOf(pagingId))
    }

    @Query("delete from amity_paging_id where position > :position and hash = :hash and nonce = :nonce")
    fun deleteAfterPosition(position: Int, hash: Int, nonce: Int): Completable

    fun deleteAfterPosition(
        position: Int,
        queryParameters: Map<String, Any>,
        nonce: Int
    ): Completable {
        return deleteAfterPosition(position, queryParameters.hashCode(), nonce)
    }

    @Query("delete from amity_query_token where pageNumber > :pageNumber and hash = :hash and nonce = :nonce")
    fun deleteAfterPageNumber(pageNumber: Int, hash: Int, nonce: Int): Completable

    fun deleteAfterPageNumber(
        pageNumber: Int,
        queryParameters: Map<String, Any>,
        nonce: Int
    ): Completable {
        return deleteAfterPageNumber(pageNumber, queryParameters.hashCode(), nonce)
    }

    @Query("select exists (SELECT 1 FROM amity_paging_id where hash = :hash and nonce = :nonce and id = :id)")
    fun pagingIdExists(hash: Int, nonce: Int, id: String): Single<Boolean>

    @Query("delete from amity_paging_id where hash = :hash and nonce = :nonce")
    fun clearPagingIds(hash: Int, nonce: Int): Completable

    fun clearPagingIds(queryParameters: Map<String, Any>, nonce: Int): Completable {
        return clearPagingIds(queryParameters.hashCode(), nonce)
    }

    @Query("delete from amity_query_token where hash = :hash and nonce = :nonce")
    fun clearQueryToken(hash: Int, nonce: Int): Completable

    fun clearQueryToken(queryParameters: Map<String, Any>, nonce: Int): Completable {
        return clearQueryToken(queryParameters.hashCode(), nonce)
    }

    @Query("select * from amity_query_token where hash = :hash and nonce = :nonce order by pageNumber asc limit 1")
    fun getFirstQueryToken(hash: Int, nonce: Int): Maybe<AmityQueryToken>


    fun getFirstQueryToken(queryParameters: Map<String, Any>, nonce: Int): Maybe<AmityQueryToken> {
        return getFirstQueryToken(queryParameters.hashCode(), nonce).filter { it.previous != null }
    }

    @Query("select * from amity_query_token where hash = :hash and nonce = :nonce order by pageNumber desc limit 1")
    fun getLastQueryToken(hash: Int, nonce: Int): Maybe<AmityQueryToken>

    fun getLastQueryToken(queryParameters: Map<String, Any>, nonce: Int): Maybe<AmityQueryToken> {
        return getLastQueryToken(queryParameters.hashCode(), nonce).filter { it.next != null }
    }

    @Query("select * from amity_query_token where pageNumber = :pageNumber and hash = :hash and nonce = :nonce limit 1")
    fun getTokenByPageNumber(pageNumber: Int, hash: Int, nonce: Int): Maybe<AmityQueryToken>

    fun getTokenByPageNumber(
        pageNumber: Int,
        queryParameters: Map<String, Any>,
        nonce: Int
    ): Maybe<String> {
        return getTokenByPageNumber(pageNumber - 1, queryParameters.hashCode(), nonce)
            .filter { it.next != null }
            .map<String> { it.next }
            .switchIfEmpty(getTokenByPageNumber(pageNumber + 1, queryParameters.hashCode(), nonce)
                .filter { it.previous != null }
                .map<String> { it.previous })
    }
}