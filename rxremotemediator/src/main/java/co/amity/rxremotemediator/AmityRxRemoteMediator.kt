package co.amity.rxremotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.rxjava2.RxRemoteMediator

@ExperimentalPagingApi
abstract class AmityRxRemoteMediator<ENTITY : Any> : RxRemoteMediator<Int, ENTITY>() {

    open fun stackFromEnd(): Boolean = false
}