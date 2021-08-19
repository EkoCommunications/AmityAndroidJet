package co.amity.android.rxremotemediator

import co.amity.rxremotemediator.AmityQueryToken

class BookQueryToken(var title: String, var category: String, next: String? = null, previous: String? = null, ids: List<String>) :
    AmityQueryToken(
        queryParameters = mapOf("title" to title, "category" to category),
        next = next,
        previous = previous,
        ids = ids
    )