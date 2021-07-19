package co.amity.android.rxremotemediator

import androidx.room.Entity
import co.amity.rxremotemediator.AmityQueryToken

@Entity(
    tableName = "book_query_token",
    primaryKeys = ["title", "category", "pageNumber"]
)
class BookQueryToken(var title: String, var category: String, ids: List<String>, next: String?, previous: String?) : AmityQueryToken(ids, next, previous)