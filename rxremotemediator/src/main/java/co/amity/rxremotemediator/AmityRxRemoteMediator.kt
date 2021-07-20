package co.amity.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.rxjava2.RxRemoteMediator

const val INVALID_PAGE_NUMBER = -1

@ExperimentalPagingApi
abstract class AmityRxRemoteMediator<ENTITY : Any> : RxRemoteMediator<Int, ENTITY>() {

    abstract fun stackFromEnd(): Boolean
}