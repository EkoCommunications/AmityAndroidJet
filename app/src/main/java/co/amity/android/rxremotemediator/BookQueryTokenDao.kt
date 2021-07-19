package co.amity.android.rxremotemediator

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import co.amity.rxremotemediator.AmityQueryTokensDao
import io.reactivex.Maybe

@Dao
interface BookQueryTokenDao : AmityQueryTokensDao<BookQueryToken> {

    @RawQuery(observedEntities = [Book::class])
    override fun queryToken(query: SimpleSQLiteQuery): Maybe<BookQueryToken>

    override fun tableName(): String {
        return "book_query_tokens"
    }
}