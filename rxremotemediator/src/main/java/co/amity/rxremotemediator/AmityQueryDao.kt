package co.amity.rxremotemediator

import android.content.Context
import androidx.room.RoomDatabase
import co.amity.rxremotemediator.provider.ContextProvider
import io.reactivex.Completable

interface AmityQueryDao {

    fun roomDatabase(context: Context): RoomDatabase

    fun tableName(): String

    fun deleteTokensAfterPageNumber(queryParameters: Map<String, Any>, pageNumber: Int): Completable {
        return ContextProvider.context
            .flatMapCompletable {
                Completable.fromAction {
                    roomDatabase(it)
                        .openHelper
                        .writableDatabase
                        .execSQL(
                            String.format(
                                "delete from %s where %s and pageNumber > %s",
                                tableName(),
                                condition(queryParameters),
                                pageNumber
                            )
                        )
                }
            }
    }

    fun condition(queryParameters: Map<String, Any>): String {
        return queryParameters
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