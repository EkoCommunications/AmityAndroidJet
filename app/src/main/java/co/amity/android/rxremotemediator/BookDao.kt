package co.amity.android.rxremotemediator

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import co.amity.rxremotemediator.AmityPagingDao
import co.amity.rxremotemediator.AmityPagingId
import io.reactivex.Completable

@Dao
interface BookDao : AmityPagingDao<Book> {

    @RawQuery(observedEntities = [Book::class, AmityPagingId::class])
    override fun queryPagingData(sqlQuery: SimpleSQLiteQuery): PagingSource<Int, Book>

    fun queryBooks(title: String, category: String): PagingSource<Int, Book> {
        return queryPagingData(
            generateSqlQuery(
                tableName = "book",
                uniqueIdKey = "id",
                queryParameters = mapOf("title" to title, "category" to category)
            )
        )
    }

//    @Query("select * from book where title = :title and category = :category order by title")
//    fun queryBooks(title: String, category: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>): Completable
}