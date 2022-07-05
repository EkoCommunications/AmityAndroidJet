package co.amity.android.data.repository.datastore

import androidx.paging.ExperimentalPagingApi
import co.amity.android.data.model.Book
import co.amity.android.data.repository.datastore.local.api.BookDao
import co.amity.android.data.repository.datastore.remote.BookRemoteDataStore
import co.amity.rxremotemediator.AmityQueryTokenDao
import co.amity.rxremotemediator.ItemKeyedRxRemoteMediator
import co.amity.rxremotemediator.PagingCursor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class BookItemKeyedRxRemoteMediator(
    private val title: String,
    private val category: String,
    private val bookDao: BookDao,
    tokenDao: AmityQueryTokenDao
) : ItemKeyedRxRemoteMediator<Book>(
    nonce = Book.NONCE,
    queryParameters = mapOf("title" to title, "category" to category),
    tokenDao = tokenDao
) {

    override fun forceRefresh(): Boolean = true

    override fun fetchFirstPage(pageSize: Int): Single<PagingCursor> {
        return BookRemoteDataStore().fetchFirstPage(
            title = title,
            category = category,
            pageSize = pageSize
        ).flatMap {
            // insert books into database and return token
            val books = it["books"].asJsonArray
            val type = object : TypeToken<List<Book>>() {}.type
            bookDao.insertBooks(Gson().fromJson(books, type))
                .andThen(Single.just(
                    PagingCursor(
                        lastCursorId = books.last().asJsonObject["bookId"].asString,
                        primaryKeys = books.map { book -> book.asJsonObject["bookId"].asString }

                    )
                ))
        }
    }

    override fun fetchByCursor(cursorId: String): Single<PagingCursor> {
        return BookRemoteDataStore().fetchByCursor(cursorId = cursorId)
            .flatMap {
                // insert books into database and return token
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        if (books.asJsonArray.size() == 0) {
                            Single.just(
                                PagingCursor(
                                    lastCursorId = "",
                                    primaryKeys = emptyList()
                                )
                            )
                        } else {
                            Single.just(
                                PagingCursor(
                                    lastCursorId = books.last().asJsonObject["bookId"].asString,
                                    primaryKeys = books.map { book -> book.asJsonObject["bookId"].asString }
                                ))
                        }
                    )
            }
            .delay(1, TimeUnit.SECONDS)
    }
}