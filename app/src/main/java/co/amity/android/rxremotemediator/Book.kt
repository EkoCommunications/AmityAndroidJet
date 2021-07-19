package co.amity.android.rxremotemediator

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "book",
    primaryKeys = ["bookId"],
    indices = [Index(value = ["title", "category"])]
)
class Book(var bookId: String, var title: String, var category: String)