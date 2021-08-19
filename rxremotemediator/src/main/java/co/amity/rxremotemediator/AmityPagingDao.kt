package co.amity.rxremotemediator

import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery

interface AmityPagingDao<ENTITY : Any> {

    fun queryPagingData(sqlQuery: SimpleSQLiteQuery): PagingSource<Int, ENTITY>

        return SimpleSQLiteQuery(
            String.format(
                "select %s.*, amity_paging_id.* from %s, amity_paging_id where amity_paging_id.id = '%s.%s' and amity_paging_id.hash = %s and amity_paging_id.nonce = %s order by amity_paging_id.position %s",
                tableName,
                tableName,
                tableName,
                idKey,
                nonce,
                queryParameters.hashCode(),
                order.value
            )
        )
    }

    enum class Order(val value: String) {
        ASC("asc"),
        DESC("desc")
    }
}