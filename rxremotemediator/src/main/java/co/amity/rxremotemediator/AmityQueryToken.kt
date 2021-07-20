package co.amity.rxremotemediator

import androidx.room.Entity
import androidx.room.Ignore

@Entity(
    tableName = "amity_query_token",
    primaryKeys = ["hash", "pageNumber"]
)
open class AmityQueryToken(@Ignore var queryParameters: Map<String, Any>, var next: String? = null, var previous: String? = null) {

    var hash: Int = queryParameters.hashCode()
    var nonce: Int = javaClass.simpleName.hashCode()
    var pageNumber: Int = INVALID_PAGE_NUMBER
}