package co.amity.android.data.repository.datastore

import androidx.paging.ExperimentalPagingApi
import co.amity.android.data.model.Book
import co.amity.android.data.model.BookQueryParams
import co.amity.android.data.repository.datastore.local.api.BookDao
import co.amity.rxremotemediator.AmityQueryParamsDao
import co.amity.rxremotemediator.PositionalRxRemoteMediator
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.reactivex.Single

@ExperimentalPagingApi
class BookPositionalRxRemoteMediator(private val title: String, private val category: String, private val bookDao: BookDao, paramsDao: AmityQueryParamsDao) :
    PositionalRxRemoteMediator<Book, BookQueryParams>(
        nonce = Book.NONCE,
        queryParameters = mapOf("title" to title, "category" to category),
        paramsDao = paramsDao
    ) {

    private fun fetchBySkipAndLimit(skip: Int, limit: Int): Single<JsonObject> {
        TODO("Not yet implemented")
    }

    override fun fetch(skip: Int, limit: Int): Single<BookQueryParams> {
        return fetchBySkipAndLimit(skip, limit)
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
                                primaryKeys = books.map { book -> book.asJsonObject["id"].asString }
                            )
                        )
                    )
            }
    }
}