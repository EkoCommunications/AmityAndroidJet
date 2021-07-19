package co.amity.android.rxremotemediator

import androidx.room.Dao
import co.amity.rxremotemediator.AmityQueryParamsDao

@Dao
interface BookQueryParamsDao : AmityQueryParamsDao<BookQueryParams> {

    override fun tableName(): String {
        return "book_query_params"
    }
}