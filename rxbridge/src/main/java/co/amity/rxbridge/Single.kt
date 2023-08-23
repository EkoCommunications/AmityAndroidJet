package co.amity.rxbridge

import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.Single as SingleV2
import io.reactivex.rxjava3.core.Single as SingleV3

fun <T : Any> SingleV3<T>.toRx2(): SingleV2<T> {
    return this.to(RxJavaBridge.toV2Single())
}

fun <T : Any> SingleV2<T>.toRx3(): SingleV3<T> {
    return this.`as`(RxJavaBridge.toV3Single())
}
