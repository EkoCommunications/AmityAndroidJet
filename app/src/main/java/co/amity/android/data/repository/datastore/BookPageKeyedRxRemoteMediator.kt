package co.amity.android.data.repository.datastore

import androidx.paging.ExperimentalPagingApi
import co.amity.android.data.model.Book
import co.amity.android.data.model.BookQueryToken
import co.amity.android.data.repository.datastore.local.api.BookDao
import co.amity.android.data.repository.datastore.remote.BookRemoteDataStore
import co.amity.android.data.repository.datastore.remote.MAX_PAGE_NUMBER
import co.amity.rxremotemediator.AmityQueryTokenDao
import co.amity.rxremotemediator.PageKeyedRxRemoteMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single

@OptIn(ExperimentalPagingApi::class)
class BookPageKeyedRxRemoteMediator(
    private val title: String,
    private val category: String,
    private val bookDao: BookDao,
    tokenDao: AmityQueryTokenDao
) : PageKeyedRxRemoteMediator<Book, BookQueryToken>(
    nonce = Book.NONCE,
    queryParameters = mapOf("title" to title, "category" to category),
    tokenDao = tokenDao
) {

    override fun fetchFirstPage(pageSize: Int): Single<BookQueryToken> {
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
                    BookQueryToken(
                        title = title,
                        category = category,
                        next = it.get("next")?.asString,
                        previous = it.get("previous")?.asString,
                        primaryKeys = books.map { book -> book.asJsonObject["bookId"].asString }
                    ).apply {
                        pageNumber = MAX_PAGE_NUMBER
                    }
                ))
        }
    }

    override fun fetchByToken(token: String): Single<BookQueryToken> {
        return BookRemoteDataStore().fetchByToken(token = token)
            .flatMap {
                // insert books into database and return token
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(Single.just(
                        BookQueryToken(
                            title = title,
                            category = category,
                            next = it.get("next")?.asString,
                            previous = it.get("previous")?.asString,
                            primaryKeys = books.map { book -> book.asJsonObject["bookId"].asString }
                        ).apply {
                            pageNumber = token.toInt()
                        }
                    ))
            }
    }
}