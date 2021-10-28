package co.amity.android.data.repository.datastore.remote

import co.amity.android.data.repository.DEFAULT_PAGE_SIZE
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Single
import java.util.*

private const val MAX_PAGE_SIZE = 10

class BookRemoteDataStore {

    fun fetchFirstPage(title: String, category: String, pageSize: Int): Single<JsonObject> {
        return Single.just(JsonObject().apply {
            addProperty("next", "2")
            add("books", JsonArray()
                .apply {
                    for (index in 1..MAX_PAGE_SIZE) {
                        add(JsonObject().apply {
                            addProperty("bookId", index.toString())
                            addProperty("title", UUID.randomUUID().toString())
                            addProperty("category", UUID.randomUUID().toString())
                        })
                    }
                })
        })
    }

    fun fetchNextPage(token: String): Single<JsonObject> {
        return Single.just(JsonObject().apply {
            if (token.toInt() < MAX_PAGE_SIZE) {
                addProperty("next", (token.toInt() + 1).toString())
            }
            if (token.toInt() > 1) {
                addProperty("previous", (token.toInt() - 1).toString())
            }
            if (token.toInt() <= MAX_PAGE_SIZE) {
                add("books", JsonArray()
                    .apply {
                        for (index in 1..MAX_PAGE_SIZE) {
                            add(JsonObject().apply {
                                addProperty("bookId", (((token.toInt() - 1) * DEFAULT_PAGE_SIZE) + index).toString())
                                addProperty("title", UUID.randomUUID().toString())
                                addProperty("category", UUID.randomUUID().toString())
                            })
                        }
                    })
            }
        })
    }
}