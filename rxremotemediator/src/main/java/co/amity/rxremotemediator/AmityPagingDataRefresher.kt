package co.amity.rxremotemediator

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AmityPagingDataRefresher(private val pageSize: Int) : RecyclerView.OnScrollListener() {

    private var pageNumber = INVALID_PAGE_NUMBER

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val linearLayoutManager = recyclerView.layoutManager
            ?.takeIf { it is LinearLayoutManager }
            ?.let { it as LinearLayoutManager } ?: return

        val lastItem = linearLayoutManager.findLastCompletelyVisibleItemPosition()
        val pageNumber = lastItem / pageSize

        if (this.pageNumber != pageNumber) {
            this.pageNumber = pageNumber

            val adapter = recyclerView.adapter
                ?.takeIf { it is PagingDataAdapter<*, *> }
                ?.let { it as PagingDataAdapter<*, *> } ?: return

            adapter.refresh()
        }
    }
}