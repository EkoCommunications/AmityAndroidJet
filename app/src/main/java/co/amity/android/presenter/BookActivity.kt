package co.amity.android.presenter

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.amity.android.data.model.Book
import co.amity.android.data.repository.DEFAULT_PAGE_SIZE
import co.amity.android.databinding.ActivityMainBinding
import co.amity.presentation.ViewBindingActivity
import co.amity.rxlifecycle.untilLifecycleEnd
import co.amity.rxremotemediator.AmityPagingDataRefresher
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

        val stackFromEnd = true

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = stackFromEnd

        binding.bookRecyclerView.layoutManager = layoutManager
        binding.bookRecyclerView.adapter = adapter
        binding.bookRecyclerView.addOnScrollListener(AmityPagingDataRefresher(stackFromEnd = true, pageSize = DEFAULT_PAGE_SIZE))
        binding.bookRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Log.e("testtest", layoutManager.findFirstCompletelyVisibleItemPosition().toString())
            }
        })

        viewModel.getAllBooks(context = this, title = "", category = "", stackFromEnd = stackFromEnd)
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