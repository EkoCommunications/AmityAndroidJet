package co.amity.android.data.repository.datastore.local

import android.content.Context
import androidx.paging.PagingSource
import co.amity.android.data.model.Book

class BookLocalDataStore {

    fun getAllBooks(context: Context, title: String, category: String, stackFromEnd: Boolean): PagingSource<Int, Book> {
        return BookDatabase.invoke(context = context)
            .bookDao()
            .getAllBooks(title = title, category = category, stackFromEnd = stackFromEnd)
    }
}