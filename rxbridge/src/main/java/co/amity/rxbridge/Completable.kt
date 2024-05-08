package co.amity.rxbridge

import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.Completable as CompletableV2
import io.reactivex.rxjava3.core.Completable as CompletableV3

fun CompletableV3.toRx2(): CompletableV2 {
    return this.to(RxJavaBridge.toV2Completable())
}

fun CompletableV2.toRx3(): CompletableV3 {
    return this.`as`(RxJavaBridge.toV3Completable())
}
