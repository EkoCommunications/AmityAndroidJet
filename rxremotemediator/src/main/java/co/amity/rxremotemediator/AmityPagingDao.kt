package co.amity.rxremotemediator

import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery

interface AmityPagingDao<ENTITY : Any> {

    fun queryPagingData(sqlQuery: SimpleSQLiteQuery): PagingSource<Int, ENTITY>

    fun generateSqlQuery(
        tableName: String,
        primaryKeyColumnName: String,
        additionalPrimaryKeys: Map<String, Any> = emptyMap(),
        queryParameters: Map<String, Any>,
        nonce: Int,
        order: Order = Order.ASC
    ): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(
            String.format(
                "select %s.*, amity_paging_id.* from %s, amity_paging_id where amity_paging_id.id = %s.%s %s and amity_paging_id.hash = %s and amity_paging_id.nonce = %s order by amity_paging_id.position %s",
                tableName,
                tableName,
                tableName,
                primaryKeyColumnName,
                additionalPrimaryKeys.takeIf { it.isNotEmpty() }?.map {
                    when (val value = it.value) {
                        is String -> String.format("%s = '%s'", it.key, value)
                        is Boolean -> String.format("%s = '%s'", it.key, if (value) 1 else 0)
                        else -> String.format("%s = %s", it.key, it.value)
                    }
                }?.joinToString(separator = ", ", prefix = "and ") ?: "",
                queryParameters.hashCode(),
                nonce,
                order.value
            )
        )
    }

    enum class Order(val value: String) {
        ASC("asc"),
        DESC("desc")
    }
}