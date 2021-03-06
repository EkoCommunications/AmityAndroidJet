package co.amity.rxremotemediator

import androidx.room.Entity
import androidx.room.Ignore

@Entity(
    tableName = "amity_query_token",
    primaryKeys = ["hash", "nonce", "pageNumber"]
)
open class AmityQueryToken(
    @Ignore var queryParameters: Map<String, Any> = emptyMap(),
    @Ignore var primaryKeys: List<String> = emptyList(),
    var next: String? = null,
    var previous: String? = null,
    var pageNumber: Int = INVALID_PAGE_NUMBER
) {

    var hash: Int = queryParameters.hashCode()
    var nonce: Int = DEFAULT_NONCE
}