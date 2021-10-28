package co.amity.android.presenter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import co.amity.android.data.model.Book

class BookAdapter(diffCallback: DiffUtil.ItemCallback<Book>) : PagingDataAdapter<Book, BookViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(context = parent.context)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        getItem(position)?.let {
            holder.onBind(it)
        }
    }
}