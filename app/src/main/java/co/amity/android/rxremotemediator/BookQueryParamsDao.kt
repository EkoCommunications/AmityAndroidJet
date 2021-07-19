package co.amity.android.rxremotemediator

import android.content.Context
import androidx.room.Dao
import androidx.room.RoomDatabase
import co.amity.rxremotemediator.AmityQueryParamsDao

@Dao
interface BookQueryParamsDao : AmityQueryParamsDao<BookQueryParams> {

    override fun roomDatabase(context: Context): RoomDatabase {
        TODO("Not yet implemented")
    }

    override fun tableName(): String {
        return "book_query_params"
    }
}