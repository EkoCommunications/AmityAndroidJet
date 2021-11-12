package co.amity.android.data.repository

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import co.amity.android.data.model.Book
import co.amity.android.data.repository.datastore.BookPageKeyedRxRemoteMediator
import co.amity.android.data.repository.datastore.local.BookDatabase
import co.amity.android.data.repository.datastore.local.BookLocalDataStore
import io.reactivex.Flowable

internal const val DEFAULT_PAGE_SIZE = 10

@OptIn(ExperimentalPagingApi::class)
class BookRepository {

    fun getAllBooks(context: Context, title: String, category: String): Flowable<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = DEFAULT_PAGE_SIZE),
            initialKey = null,
            remoteMediator = BookPageKeyedRxRemoteMediator(
                title = title,
                category = category,
                bookDao = BookDatabase.invoke(context = context).bookDao(),
                tokenDao = BookDatabase.invoke(context = context).tokenDao()
            )
        ) {
            BookLocalDataStore().getAllBooks(
                context = context,
                title = title,
                category = category
            )
        }.flowable
    }
}