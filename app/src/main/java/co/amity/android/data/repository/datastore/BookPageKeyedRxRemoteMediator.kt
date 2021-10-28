package co.amity.android.data.repository.datastore

import androidx.paging.ExperimentalPagingApi
import co.amity.android.data.model.Book
import co.amity.android.data.model.BookQueryToken
import co.amity.android.data.repository.datastore.local.api.BookDao
import co.amity.android.data.repository.datastore.remote.BookRemoteDataStore
import co.amity.rxremotemediator.AmityQueryTokenDao
import co.amity.rxremotemediator.PageKeyedRxRemoteMediator
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.reactivex.Single

@OptIn(ExperimentalPagingApi::class)
class BookPageKeyedRxRemoteMediator(
    private val title: String,
    private val category: String,
    private val stackFromEnd: Boolean,
    private val bookDao: BookDao,
    tokenDao: AmityQueryTokenDao
) : PageKeyedRxRemoteMediator<Book, BookQueryToken>(
    nonce = Book.NONCE,
    queryParameters = mapOf("title" to title, "category" to category),
    tokenDao = tokenDao
) {

    private fun fetchFirstPage(title: String, category: String, pageSize: Int): Single<JsonObject> {
        return BookRemoteDataStore().fetchFirstPage(
            title = title,
            category = category,
            pageSize = pageSize,
            stackFromEnd = stackFromEnd
        )
    }

    private fun fetchNextPage(token: String): Single<JsonObject> {
        return BookRemoteDataStore().fetchNextPage(token = token, stackFromEnd = stackFromEnd)
    }

    override fun fetchFirstPage(pageSize: Int): Single<BookQueryToken> {
        return fetchFirstPage(title, category, pageSize)
            .flatMap {
                // insert books into database and return token
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Single.just(
                            BookQueryToken(
                                title = title,
                                category = category,
                                next = it.get("next")?.asString,
                                previous = null,
                                primaryKeys = books.map { book -> book.asJsonObject["bookId"].asString }
                            )
                        )
                    )
            }
    }

    override fun fetch(token: String): Single<BookQueryToken> {
        return fetchNextPage(token)
            .flatMap {
                // insert books into database and return token
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Single.just(
                            BookQueryToken(
                                title = title,
                                category = category,
                                next = it.get("next")?.asString,
                                previous = it.get("previous")?.asString,
                                primaryKeys = books.map { book -> book.asJsonObject["bookId"].asString }
                            )
                        )
                    )
            }
    }

    override fun stackFromEnd(): Boolean {
        return stackFromEnd
    }
}