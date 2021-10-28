package co.amity.android.presenter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import co.amity.android.data.model.Book
import co.amity.android.domain.GetAllBookUseCase
import io.reactivex.Flowable

class BookViewModel : ViewModel() {

    fun getAllBooks(context: Context, title: String, category: String, stackFromEnd: Boolean): Flowable<PagingData<Book>> {
        return GetAllBookUseCase().getAllBooks(context = context, title = title, category = category, stackFromEnd = stackFromEnd)
    }
}