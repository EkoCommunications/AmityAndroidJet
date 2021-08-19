package co.amity.android.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import co.amity.rxremotemediator.AmityQueryTokenDao
import co.amity.rxremotemediator.PageKeyedRxRemoteMediator
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.reactivex.Single

@ExperimentalPagingApi
class BookPageKeyedRxRemoteMediator(private val title: String, private val category: String, private val bookDao: BookDao, tokenDao: AmityQueryTokenDao) :
    PageKeyedRxRemoteMediator<Book, BookQueryToken>(
        nonce = Book.NONCE,
        queryParameters = mapOf("title" to title, "category" to category),
        tokenDao = tokenDao
    ) {

    private fun fetchBooksByTitleAndCategory(title: String, category: String, pageSize: Int): Single<JsonObject> {
        TODO("Not yet implemented")
    }

    private fun fetchBooksByToken(token: String): Single<JsonObject> {
        TODO("Not yet implemented")
    }

    override fun fetchFirstPage(pageSize: Int): Single<BookQueryToken> {
        return fetchBooksByTitleAndCategory(title, category, pageSize)
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
                                next = it.get("next").asString,
                                previous = null,
                                ids = books.map { book -> book.asJsonObject["id"].asString }
                            )
                        )
                    )
            }
    }

    override fun fetch(token: String): Single<BookQueryToken> {
        return fetchBooksByToken(token)
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
                                next = it.get("next").asString,
                                previous = it.get("previous").asString,
                                ids = books.map { book -> book.asJsonObject["id"].asString }
                            )
                        )
                    )
            }
    }

    override fun stackFromEnd(): Boolean {
        return false
    }
}