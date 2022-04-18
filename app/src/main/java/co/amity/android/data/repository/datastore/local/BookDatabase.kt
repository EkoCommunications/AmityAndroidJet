package co.amity.android.data.repository.datastore.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import co.amity.android.data.model.Book
import co.amity.android.data.repository.datastore.local.api.BookDao
import co.amity.rxremotemediator.AmityPagingId
import co.amity.rxremotemediator.AmityQueryToken
import co.amity.rxremotemediator.AmityQueryTokenDao

@Database(version = 1, entities = [Book::class, AmityQueryToken::class, AmityPagingId::class])
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    abstract fun tokenDao(): AmityQueryTokenDao

    companion object {

        @Volatile
        private var instance: BookDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, BookDatabase::class.java, "book.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}