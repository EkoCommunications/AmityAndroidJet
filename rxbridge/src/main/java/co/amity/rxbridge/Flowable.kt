package co.amity.rxbridge

import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.Flowable as FlowableV2
import io.reactivex.rxjava3.core.Flowable as FlowableV3


fun <T : Any> FlowableV3<T>.toRx2(): FlowableV2<T> {
    return this.to(RxJavaBridge.toV2Flowable())
}

fun <T : Any> FlowableV2<T>.toRx3(): FlowableV3<T> {
    return this.`as`(RxJavaBridge.toV3Flowable())
}