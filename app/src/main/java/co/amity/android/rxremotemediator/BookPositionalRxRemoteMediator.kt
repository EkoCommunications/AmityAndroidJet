package co.amity.android.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import co.amity.rxremotemediator.PositionalRemoteMediator
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.reactivex.Single

@ExperimentalPagingApi
class BookPositionalRxRemoteMediator(private val title: String, private val category: String, private val bookDao: BookDao, paramsDao: BookQueryParamsDao) :
    PositionalRemoteMediator<Book, BookQueryParams, BookQueryParamsDao>(paramsDao) {

    private fun queryBySkipAndLimit(skip: Int, limit: Int): Single<JsonObject> {
        TODO("Not yet implemented")
    }

    override fun fetch(skip: Int, limit: Int): Single<Array<BookQueryParams>> {
        return queryBySkipAndLimit(skip, limit)
            .flatMap {
                // insert books into database and return tokens
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(Single.just(books.mapIndexed { index, book ->
                        BookQueryParams(
                            title = book.asJsonObject["title"].asString,
                            category = book.asJsonObject["category"].asString,
                            position = skip + index + 1,
                            endOfPaginationReached = it.size() - 1 == index && it.size() < limit
                        )
                    }.toTypedArray()))
            }
    }

    override fun queryParameters(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "category" to category
        )
    }
}