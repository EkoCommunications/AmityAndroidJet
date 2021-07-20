package co.amity.rxremotemediator

import androidx.room.Entity
import androidx.room.Ignore

@Entity(
    tableName = "amity_query_params",
    primaryKeys = ["hash", "pageNumber"]
)
open class AmityQueryParams(@Ignore var queryParameters: Map<String, Any>, var endOfPaginationReached: Boolean) {

    var hash: Int = queryParameters.hashCode()
    var nonce: Int = javaClass.hashCode()
    var pageNumber: Int = INVALID_PAGE_NUMBER
}