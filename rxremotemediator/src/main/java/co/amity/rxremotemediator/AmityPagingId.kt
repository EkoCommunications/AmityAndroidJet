package co.amity.rxremotemediator

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index

@Entity(
    tableName = "amity_paging_id",
    primaryKeys = ["hash", "nonce", "position"],
    indices = [Index(value = ["id"], unique = true)]
)
class AmityPagingId(
    @Ignore var queryParameters: Map<String, Any> = emptyMap(),
    var id: String? = null
) {

    var hash: Int = queryParameters.hashCode()
    var nonce: Int = DEFAULT_NONCE
    var position: Int = INVALID_POSITION
}