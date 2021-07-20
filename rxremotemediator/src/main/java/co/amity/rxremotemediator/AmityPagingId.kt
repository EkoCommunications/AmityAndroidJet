package co.amity.rxremotemediator

import androidx.room.Entity

@Entity(
    tableName = "amity_paging_id",
    primaryKeys = ["hash", "position"]
)
class AmityPagingId(var id: String, var hash: Int, var position: Int)