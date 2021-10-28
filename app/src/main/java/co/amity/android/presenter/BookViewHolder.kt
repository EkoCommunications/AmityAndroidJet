package co.amity.android.presenter

import android.content.Context
import co.amity.android.R
import co.amity.android.data.model.Book
import co.amity.android.databinding.ViewHolderBookBinding
import co.amity.presentation.ViewBindingViewHolder

class BookViewHolder(context: Context) : ViewBindingViewHolder<Book, ViewHolderBookBinding>(
    context = context,
    resource = R.layout.view_holder_book
) {

    override fun generateViewBinding(): ViewHolderBookBinding {
        return ViewHolderBookBinding.bind(itemView)
    }

    override fun onBind(item: Book) {
        binding.viewHolderId.text = layoutPosition.toString()
        binding.viewHolderTitle.text = item.title
        binding.viewHolderCategory.text = item.category
    }
}