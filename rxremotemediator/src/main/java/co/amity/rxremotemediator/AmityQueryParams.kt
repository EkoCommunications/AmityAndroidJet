package co.amity.rxremotemediator

import androidx.room.Entity
import androidx.room.Ignore

@Entity(
    tableName = "amity_query_params",
    primaryKeys = ["hash", "nonce", "pageNumber"]
)
open class AmityQueryParams(@Ignore var queryParameters: Map<String, Any>, @Ignore var uniqueIds: List<String>, var endOfPaginationReached: Boolean) {

    var hash: Int = queryParameters.hashCode()
    var nonce: Int = DEFAULT_NONCE
    var pageNumber: Int = INVALID_PAGE_NUMBER
}