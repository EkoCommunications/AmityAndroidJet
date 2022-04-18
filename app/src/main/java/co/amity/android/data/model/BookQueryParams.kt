package co.amity.android.data.model

import co.amity.rxremotemediator.AmityQueryParams

class BookQueryParams(var title: String, var category: String, endOfPaginationReached: Boolean, primaryKeys: List<String>) :
    AmityQueryParams(
        queryParameters = mapOf("title" to title, "category" to category),
        endOfPaginationReached = endOfPaginationReached,
        primaryKeys = primaryKeys
    )