package co.amity.rxremotemediator

open class AmityQueryParams(var ids: List<String>, var endOfPaginationReached: Boolean = false, var pageNumber: Int = INVALID_PAGE_NUMBER)