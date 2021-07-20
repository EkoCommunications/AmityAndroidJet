package co.amity.rxremotemediator

import androidx.room.Entity
import androidx.room.Ignore

@Entity(
    tableName = "amity_paging_id",
    primaryKeys = ["hash", "position"]
)
class AmityPagingId(@Ignore private var queryParameters: Map<String, Any>, var uniqueId: String) {

    var hash: Int = queryParameters.hashCode()
    var nonce: Int? = null
    var position: Int = INVALID_POSITION
}