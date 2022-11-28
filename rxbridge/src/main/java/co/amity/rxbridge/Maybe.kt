package co.amity.rxbridge

import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.Maybe as MaybeV2
import io.reactivex.rxjava3.core.Maybe as MaybeV3

fun <T : Any> MaybeV3<T>.toRx2(): MaybeV2<T> {
    return this.to(RxJavaBridge.toV2Maybe())
}

fun <T : Any> MaybeV2<T>.toRx3(): MaybeV3<T> {
    return this.`as`(RxJavaBridge.toV3Maybe())
}