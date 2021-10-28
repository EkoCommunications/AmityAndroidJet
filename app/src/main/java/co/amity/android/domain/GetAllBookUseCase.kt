package co.amity.android.domain

import android.content.Context
import androidx.paging.PagingData
import co.amity.android.data.model.Book
import co.amity.android.data.repository.BookRepository
import io.reactivex.Flowable

class GetAllBookUseCase {

    fun getAllBooks(context: Context, title: String, category: String, stackFromEnd: Boolean): Flowable<PagingData<Book>> {
        return BookRepository().getAllBooks(context = context, title = title, category = category, stackFromEnd = stackFromEnd)
    }
}