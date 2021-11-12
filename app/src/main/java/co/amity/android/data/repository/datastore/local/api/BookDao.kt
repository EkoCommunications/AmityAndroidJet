package co.amity.android.data.repository.datastore.local.api

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import co.amity.android.data.model.Book
import co.amity.rxremotemediator.AmityPagingDao
import co.amity.rxremotemediator.AmityPagingId
import io.reactivex.Completable

@Dao
interface BookDao : AmityPagingDao<Book> {

    @RawQuery(observedEntities = [Book::class, AmityPagingId::class])
    override fun queryPagingData(sqlQuery: SimpleSQLiteQuery): PagingSource<Int, Book>

    fun getAllBooks(title: String, category: String): PagingSource<Int, Book> {
        return queryPagingData(
            generateSqlQuery(
                tableName = "book",
                primaryKeyColumnName = "bookId",
                additionalPrimaryKeys = emptyMap(),
                queryParameters = mapOf("title" to title, "category" to category),
                nonce = Book.NONCE,
                order = AmityPagingDao.Order.ASC
            )
        )
    }

//    @Query("select * from book where title = :title and category = :category order by title")
//    fun queryBooks(title: String, category: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>): Completable
}