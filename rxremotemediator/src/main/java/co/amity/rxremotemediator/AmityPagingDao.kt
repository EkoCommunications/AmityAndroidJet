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
        sortColumn: String?,
        additionalFilter: String? = null
    ): SimpleSQLiteQuery {
        return generateSqlQuery(
                tableName = tableName,
                primaryKeyColumnName = primaryKeyColumnName,
                additionalPrimaryKeys = additionalPrimaryKeys,
                queryParameters = queryParameters,
                nonce = nonce,
                order = order,
                sortColumns = sortColumn?.let { listOf(Sorting.Column(tableName, it, order)) },
                additionalFilter = additionalFilter
        )
    }

    fun generateSqlQuery(
        tableName: String,
        primaryKeyColumnName: String,
        additionalPrimaryKeys: Map<String, Any> = emptyMap(),
        queryParameters: Map<String, Any>,
        nonce: Int,
        order: Order,
        sortColumns: List<Sorting>? = null,
        additionalFilter: String? = null
    ): SimpleSQLiteQuery {
        val additionalPrimaryKeyString = additionalPrimaryKeys.takeIf { it.isNotEmpty() }?.map {
            when (val value = it.value) {
                is String -> "$tableName.${it.key} = '$value'"
                is Boolean -> "$tableName.${it.key} = '${if (value) 1 else 0}'"
                else -> String.format("%s.%s = %s", tableName, it.key, it.value)
            }
        }?.joinToString(separator = " and ", prefix = "and ") ?: ""

        //base query statement
        var queryStatement = "select $tableName.*, " +
                "amity_paging_id.* from $tableName, " +
                "amity_paging_id where amity_paging_id.id = $tableName.$primaryKeyColumnName $additionalPrimaryKeyString" +
                " and " +
                "amity_paging_id.hash = ${queryParameters.hashCode()}" +
                " and " +
                "amity_paging_id.nonce = $nonce"

        //add filter
        additionalFilter?.let {
            queryStatement.plus(" and $additionalFilter")
                .also { queryStatement = it }
        }

        //add sort
        if (sortColumns?.isNotEmpty() == true) {
            queryStatement.plus(" order by ${sortColumns.joinToString(",") { it.toSql() }}")
                .also { queryStatement = it }
        } else {
            queryStatement.plus(" order by amity_paging_id.position ${order.value}")
                .also { queryStatement = it }
        }
        return SimpleSQLiteQuery(queryStatement)
    }

    enum class Order(val value: String) {
        ASC("asc"),
        DESC("desc")
    }
  
    sealed class Sorting {
    
        abstract fun toSql(): String
        class Column(
            private val tableName: String,
            private val columnName: String,
            private val order: Order
        ) : Sorting() {
            override fun toSql(): String {
                return "$tableName.$columnName ${order.value}"
            }
        }
        
        class Enum(
            private val tableName: String,
            private val columnName: String,
            private val enumList: List<String>
        ) : Sorting() {
            override fun toSql(): String {
                return convertEnumToOrder(tableName, columnName, enumList) ?: ""
            }

            private fun convertEnumToOrder(tableName: String, sortColumn: String, enumList: List<String>): String? {
                return if (enumList.isNotEmpty()) {
                    "CASE $tableName.$sortColumn " +
                            enumList.joinToString(" ") {
                                "WHEN '$it' THEN ${enumList.indexOf(it)}"
                            } +
                            " ELSE ${enumList.size}" +
                            " END ASC "
                } else {
                    null
                }
            }
        }
    }
}