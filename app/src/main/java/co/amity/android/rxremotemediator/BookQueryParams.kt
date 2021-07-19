package co.amity.android.rxremotemediator

import androidx.room.Entity
import co.amity.rxremotemediator.AmityQueryParams

@Entity(
    tableName = "book_query_params",
    primaryKeys = ["title", "category", "position"]
)
class BookQueryParams(var title: String, var category: String, endOfPaginationReached: Boolean) : AmityQueryParams(endOfPaginationReached)