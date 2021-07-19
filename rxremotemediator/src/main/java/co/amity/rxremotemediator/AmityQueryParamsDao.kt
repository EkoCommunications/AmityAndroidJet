package co.amity.rxremotemediator

import android.content.Context
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.reactivex.Completable

interface AmityQueryParamsDao<PARAMS : AmityQueryParams> : AmityQueryObjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParams(parameters: Array<PARAMS>): Completable

    fun deleteParamsAfterIndex(context: Context, queryParameters: Map<String, Any>, index: Int): Completable {
        return Completable.never()
    }
}