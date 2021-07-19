package co.amity.rxremotemediator

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Completable
import io.reactivex.Maybe

interface AmityPagingTokenDao<QUERY_TOKEN : AmityQueryToken> {

    fun getFirstQueryToken(primaryKeys: Map<String, Any>): Maybe<String> {
        return queryToken(
            SimpleSQLiteQuery(
                String.format(
                    "select * from %s where %s order by pageNumber asc limit 1",
                    tableName(), condition(primaryKeys)
                )
            )
        ).filter { it.previous != null }
            .map<String> { it.previous }
    }

    fun getLastQueryToken(primaryKeys: Map<String, Any>): Maybe<String> {
        return queryToken(
            SimpleSQLiteQuery(
                String.format(
                    "select * from %s where %s order by pageNumber desc limit 1",
                    tableName(), condition(primaryKeys)
                )
            )
        ).filter { it.next != null }
            .map<String> { it.next }
    }

    fun getTokenByPageNumber(pageNumber: Int, primaryKeys: Map<String, Any>): Maybe<String> {
        return queryToken(
            SimpleSQLiteQuery(
                String.format(
                    "select * from %s where %s and pageNumber = %s limit 1",
                    tableName(), condition(primaryKeys), pageNumber - 1
                )
            )
        ).filter { it.next != null }
            .map<String> { it.next }
            .switchIfEmpty(queryToken(
                SimpleSQLiteQuery(
                    String.format(
                        "select * from %s where %s and pageNumber = %s limit 1",
                        tableName(), condition(primaryKeys), pageNumber + 1
                    )
                )
            ).filter { it.previous != null }
                .map<String> { it.previous })
    }

    fun queryToken(query: SimpleSQLiteQuery): Maybe<QUERY_TOKEN>

    fun insertToken(token: QUERY_TOKEN): Completable

    fun tableName(): String

    private fun condition(primaryKeys: Map<String, Any>): String {
        return primaryKeys
            .map {
                when (val value = it.value) {
                    is String -> String.format("%s = '%s'", it.key, value)
                    is Boolean -> String.format("%s = '%s'", it.key, if (value) 1 else 0)
                    else -> String.format("%s = %s", it.key, it.value)
                }
            }
            .joinToString(separator = " and ")
    }
}