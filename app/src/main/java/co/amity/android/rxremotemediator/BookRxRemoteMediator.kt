package co.amity.android.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import co.amity.rxremotemediator.AmityRxRemoteMediator
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.reactivex.Maybe

@ExperimentalPagingApi
class BookRxRemoteMediator(private val title: String, private val category: String, private val bookDao: BookDao, tokenDao: BookQueryTokenDao) :
    AmityRxRemoteMediator<Book, BookQueryToken, BookQueryTokenDao>(tokenDao) {

    private fun queryByTitleAndCategory(title: String, category: String, pageSize: Int): Maybe<JsonObject> {
        TODO("Not yet implemented")
    }

    private fun queryByToken(token: String): Maybe<JsonObject> {
        TODO("Not yet implemented")
    }

    override fun fetchFirstPage(pageSize: Int): Maybe<BookQueryToken> {
        return queryByTitleAndCategory(title, category, pageSize)
            .flatMap {
                val books = it["book"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(Maybe.just(BookQueryToken(title = title, category = category, next = it.get("next").asString, previous = null)))
            }
    }

    override fun fetch(token: String, pageSize: Int): Maybe<BookQueryToken> {
        return queryByToken(token)
            .flatMap {
                // insert books into database and return tokens
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(Maybe.just(BookQueryToken(title = title, category = category, next = it.get("next").asString, previous = it.get("previous").asString)))
            }
    }

    override fun queryParameters(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "category" to category
        )
    }

    override fun applyQueryParametersToToken(token: BookQueryToken): BookQueryToken {
        return token.apply {
            this.title = this@BookRxRemoteMediator.title
            this.category = this@BookRxRemoteMediator.category
        }
    }

    override fun stackFromEnd(): Boolean {
        return true
    }
}