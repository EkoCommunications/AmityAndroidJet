package co.amity.android.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import co.amity.rxremotemediator.AmityQueryParamsDao
import co.amity.rxremotemediator.PositionalRemoteMediator
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.reactivex.Single

@ExperimentalPagingApi
class BookPositionalRxRemoteMediator(private val title: String, private val category: String, private val bookDao: BookDao, paramsDao: AmityQueryParamsDao) :
    PositionalRemoteMediator<Book, BookQueryParams>(
        nonce = "book".hashCode(),
        queryParameters = mapOf("title" to title, "category" to category),
        paramsDao = paramsDao
    ) {

    private fun queryBySkipAndLimit(skip: Int, limit: Int): Single<JsonObject> {
        TODO("Not yet implemented")
    }

    override fun fetch(skip: Int, limit: Int): Single<BookQueryParams> {
        return queryBySkipAndLimit(skip, limit)
            .flatMap {
                // insert books into database and return params
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Single.just(
                            BookQueryParams(
                                title = title,
                                category = category,
                                endOfPaginationReached = books.size() < limit,
                                uniqueIds = books.map { book -> book.asJsonObject["id"].asString }
                            )
                        )
                    )
            }
    }
}