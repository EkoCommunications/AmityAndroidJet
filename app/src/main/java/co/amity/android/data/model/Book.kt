package co.amity.android.data.model

import androidx.room.Entity

@Entity(
    tableName = "book",
    primaryKeys = ["bookId"]
)
class Book(var bookId: String, var title: String, var category: String) {

    companion object {
        const val NONCE: Int = 42
    }
}