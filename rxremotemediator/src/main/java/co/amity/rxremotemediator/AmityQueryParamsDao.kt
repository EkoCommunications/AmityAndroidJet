package co.amity.rxremotemediator

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.reactivex.Completable

interface AmityQueryParamsDao<PARAMS : AmityQueryParams> : AmityQueryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParams(parameter: PARAMS): Completable
}