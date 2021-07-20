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

    @Query("delete from amity_query_params where pageNumber > :pageNumber and hash = :hash and nonce = :nonce")
    fun deleteAfterPageNumber(pageNumber: Int, hash: Int, nonce: Int): Completable
}