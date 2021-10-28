package co.amity.android.presenter

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import co.amity.android.data.model.Book
import co.amity.android.databinding.ActivityMainBinding
import co.amity.presentation.ViewBindingActivity
import co.amity.rxlifecycle.untilLifecycleEnd
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class BookActivity : ViewBindingActivity<ActivityMainBinding>() {

    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = BookAdapter(diffCallback = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.bookId == newItem.bookId
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.title == newItem.title && oldItem.category == newItem.category
            }
        })

        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = adapter

        viewModel.getAllBooks(context = this, title = "", category = "")
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { adapter.submitData(lifecycle, it) }
            .untilLifecycleEnd(this)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    override fun generateViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
}