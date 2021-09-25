package co.amity.rxremotemediator

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
interface AmityQueryTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToken(token: AmityQueryToken): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPagingIds(pagingIds: List<AmityPagingId>): Completable

    @Query("delete from amity_query_token where pageNumber > :pageNumber and nonce = :nonce and hash = :hash")
    fun deleteAfterPageNumber(pageNumber: Int, nonce: Int, hash: Int): Completable

    fun deleteAfterPageNumber(pageNumber: Int, queryParameters: Map<String, Any>, nonce: Int): Completable {
        return deleteAfterPageNumber(pageNumber, queryParameters.hashCode(), nonce)
    }

    @Query("delete from amity_paging_id where nonce = :nonce and hash = :hash")
    fun deletePagingIds(nonce: Int, hash: Int): Completable

    fun deletePagingIds(queryParameters: Map<String, Any>, nonce: Int): Completable {
        return deletePagingIds(queryParameters.hashCode(), nonce)
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

    fun getTokenByPageNumber(pageNumber: Int, queryParameters: Map<String, Any>, nonce: Int): Maybe<String> {
        return getTokenByPageNumber(pageNumber - 1, queryParameters.hashCode(), nonce)
            .filter { it.next != null }
            .map<String> { it.next }
            .switchIfEmpty(getTokenByPageNumber(pageNumber + 1, queryParameters.hashCode(), nonce)
                .filter { it.previous != null }
                .map<String> { it.previous })
    }
}