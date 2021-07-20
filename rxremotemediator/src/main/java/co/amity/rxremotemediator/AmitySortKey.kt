package co.amity.rxremotemediator

import androidx.room.Entity

@Entity(
    tableName = "amity_sort_key",
    primaryKeys = ["hash", "id"]
)
class AmitySortKey(var hash: Int, var id: String)