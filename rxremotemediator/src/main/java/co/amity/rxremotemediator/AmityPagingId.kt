package co.amity.rxremotemediator

import androidx.room.Entity

@Entity(
    tableName = "amity_paging_id",
    primaryKeys = ["hash", "nonce", "position"]
)
class AmityPagingId(
    queryParameters: Map<String, Any> = emptyMap(),
    var id: String? = null
) {

    var hash: Int = queryParameters.hashCode()
    var nonce: Int = DEFAULT_NONCE
    var position: Int = INVALID_POSITION
}