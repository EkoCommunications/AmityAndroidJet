package co.amity.rxremotemediator

open class AmityQueryToken(var ids: List<String>, var next: String?, var previous: String?, var pageNumber: Int = INVALID_PAGE_NUMBER)