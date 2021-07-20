package co.amity.rxremotemediator

import androidx.room.Entity
import androidx.room.Ignore

@Entity(
    tableName = "amity_query_token",
    primaryKeys = ["hash", "pageNumber"]
)
open class AmityQueryToken(@Ignore private var queryParameters: Map<String, Any>, var next: String? = null, var previous: String? = null) {

    var hash: Int = queryParameters.hashCode()
    var nonce: Int? = null
    var pageNumber: Int = INVALID_PAGE_NUMBER
}