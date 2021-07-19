package co.amity.android.rxremotemediator

import android.content.Context
import androidx.room.Dao
import androidx.room.RawQuery
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import co.amity.rxremotemediator.AmityQueryTokenDao
import io.reactivex.Maybe

@Dao
interface BookQueryTokenDao : AmityQueryTokenDao<BookQueryToken> {

    @RawQuery(observedEntities = [Book::class])
    override fun queryToken(query: SimpleSQLiteQuery): Maybe<BookQueryToken>

    override fun roomDatabase(context: Context): RoomDatabase {
        TODO("Not yet implemented")
    }

    override fun tableName(): String {
        return "book_query_token"
    }
}