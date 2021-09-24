package co.amity.rxremotemediator

import androidx.room.Entity
import androidx.room.Ignore

@Entity(
    tableName = "amity_paging_id",
    primaryKeys = ["hash", "nonce", "id"]
)
class AmityPagingId(
    @Ignore var queryParameters: Map<String, Any> = emptyMap(),
    var id: String = ""
) {

    var hash: Int = queryParameters.hashCode()
    var nonce: Int = DEFAULT_NONCE
    var position: Int = INVALID_POSITION
}