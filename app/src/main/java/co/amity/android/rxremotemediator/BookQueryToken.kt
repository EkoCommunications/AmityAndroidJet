package co.amity.android.rxremotemediator

import androidx.room.Entity
import co.amity.rxremotemediator.AmityQueryTokens

@Entity(
    tableName = "book_query_tokens",
    primaryKeys = ["title", "category"]
)
class BookQueryToken(var title: String, var category: String, next: String?, previous: String?) : AmityQueryTokens(next, previous)