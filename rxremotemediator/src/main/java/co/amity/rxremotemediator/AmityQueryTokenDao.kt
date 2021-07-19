package co.amity.rxremotemediator

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Completable
import io.reactivex.Maybe

interface AmityQueryTokenDao<QUERY_TOKEN : AmityQueryToken> : AmityQueryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToken(token: QUERY_TOKEN): Completable

    fun getFirstQueryToken(primaryKeys: Map<String, Any>): Maybe<QUERY_TOKEN> {
        return queryToken(
            SimpleSQLiteQuery(
                String.format(
                    "select * from %s where %s order by pageNumber asc limit 1",
                    tableName(), condition(primaryKeys)
                )
            )
        ).filter { it.previous != null }
    }

    fun getLastQueryToken(primaryKeys: Map<String, Any>): Maybe<QUERY_TOKEN> {
        return queryToken(
            SimpleSQLiteQuery(
                String.format(
                    "select * from %s where %s order by pageNumber desc limit 1",
                    tableName(), condition(primaryKeys)
                )
            )
        ).filter { it.next != null }
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
}