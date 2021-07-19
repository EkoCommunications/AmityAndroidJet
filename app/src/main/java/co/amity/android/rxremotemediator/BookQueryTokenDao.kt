package co.amity.android.rxremotemediator

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import co.amity.rxremotemediator.AmityPagingTokenDao
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
interface BookQueryTokenDao : AmityPagingTokenDao<BookQueryToken> {

    @RawQuery(observedEntities = [Book::class])
    override fun queryToken(query: SimpleSQLiteQuery): Maybe<BookQueryToken>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insertToken(token: BookQueryToken): Completable

    override fun tableName(): String {
        return "book"
    }
}