package co.amity.rxremotemediator

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable

@Dao
interface AmityQueryParamsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParams(params: AmityQueryParams): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPagingIds(pagingIds: List<AmityPagingId>): Completable

    @Query("delete from amity_query_params where pageNumber > :pageNumber and nonce = :nonce and hash = :hash")
    fun deleteAfterPageNumber(pageNumber: Int, nonce: Int, hash: Int): Completable

    fun deleteAfterPageNumber(pageNumber: Int, queryParameters: Map<String, Any>, nonce: Int): Completable {
        return deleteAfterPageNumber(pageNumber, queryParameters.hashCode(), nonce)
    }
}