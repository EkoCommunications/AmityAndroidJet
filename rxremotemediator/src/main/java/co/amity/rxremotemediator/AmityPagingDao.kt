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
        order: Order,
        sortColumn: String? = null
    ): SimpleSQLiteQuery {
        val additionalPrimaryKeyString = additionalPrimaryKeys.takeIf { it.isNotEmpty() }?.map {
            when (val value = it.value) {
                is String -> "$tableName.${it.key} = '$value'"
                is Boolean -> "$tableName.${it.key} = '${if (value) 1 else 0}'"
                else -> String.format("%s.%s = %s", tableName, it.key, it.value)
            }
        }?.joinToString(separator = " and ", prefix = "and ") ?: ""

        var queryStatement = "select $tableName.*, " +
                "amity_paging_id.* from $tableName, " +
                "amity_paging_id where amity_paging_id.id = $tableName.$primaryKeyColumnName $additionalPrimaryKeyString" +
                " and " +
                "amity_paging_id.hash = ${queryParameters.hashCode()}" +
                " and " +
                "amity_paging_id.nonce = $nonce" +
                " order by "

        sortColumn?.let {
            queryStatement.plus("$tableName.$sortColumn ${order.value}")
                .also { queryStatement = it }
        } ?: apply {
            queryStatement.plus("amity_paging_id.position ${order.value}")
                .also { queryStatement = it }
        }
        return SimpleSQLiteQuery(queryStatement)
    }

    enum class Order(val value: String) {
        ASC("asc"),
        DESC("desc")
    }
}