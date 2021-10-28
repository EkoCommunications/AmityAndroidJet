package co.amity.android.data.repository.datastore.remote

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Single
import java.util.*

class BookRemoteDataStore {

    fun fetchFirstPage(title: String, category: String, pageSize: Int): Single<JsonObject> {
        return Single.just(JsonObject().apply {
            addProperty("next", "2")
            add("books", JsonArray()
                .apply {
                    for (index in 0..10) {
                        add(JsonObject().apply {
                            addProperty("bookId", UUID.randomUUID().toString())
                            addProperty("title", UUID.randomUUID().toString())
                            addProperty("category", UUID.randomUUID().toString())
                        })
                    }
                })
        })
    }

    fun fetchNextPage(token: String): Single<JsonObject> {
        return Single.just(JsonObject().apply {
            if (token.toInt() < 10) {
                addProperty("next", (token.toInt() + 1).toString())
            }
            if (token.toInt() > 1) {
                addProperty("previous", (token.toInt() - 1).toString())
            }
            add("books", JsonArray()
                .apply {
                    for (index in 0..10) {
                        add(JsonObject().apply {
                            addProperty("bookId", UUID.randomUUID().toString())
                            addProperty("title", UUID.randomUUID().toString())
                            addProperty("category", UUID.randomUUID().toString())
                        })
                    }
                })
        })
    }
}