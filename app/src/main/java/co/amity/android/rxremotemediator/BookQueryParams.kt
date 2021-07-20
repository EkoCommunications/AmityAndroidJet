package co.amity.android.rxremotemediator

import co.amity.rxremotemediator.AmityQueryParams

class BookQueryParams(var title: String, var category: String, endOfPaginationReached: Boolean, uniqueIds: List<String>) :
    AmityQueryParams(
        queryParameters = mapOf("title" to title, "category" to category),
        endOfPaginationReached = endOfPaginationReached,
        uniqueIds = uniqueIds
    )