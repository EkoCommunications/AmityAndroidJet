package co.amity.rxlifecycle

import android.view.View
import androidx.annotation.UiThread
import com.trello.rxlifecycle4.LifecycleProvider
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
inline fun <reified E, T : Any> Single<T>.untilLifecycleEnd(
    lifecycleProvider: LifecycleProvider<E>,
    uniqueId: String? = null
): Single<T> {
    return when (E::class) {
        ActivityEvent::class -> bindUntilEvent(
            lifecycleProvider as LifecycleProvider<ActivityEvent>,
            ActivityEvent.DESTROY
        )
        FragmentEvent::class -> bindUntilEvent(
            lifecycleProvider as LifecycleProvider<FragmentEvent>,
            FragmentEvent.DESTROY
        )
        ViewEvent::class -> bindUntilEvent(
            lifecycleProvider as LifecycleProvider<ViewEvent>,
            ViewEvent.DETACH
        )
        else -> this
    }.doOnSubscribe {
        manageSingleRx3Disposables(it, uniqueId)
    }.doOnDispose {
        removeSingleRx3Disposable(uniqueId)
    }.doOnTerminate {
        removeSingleRx3Disposable(uniqueId)
    }.allowEmpty()
}

@UiThread
fun <T : Any> Single<T>.untilLifecycleEnd(view: View, uniqueId: String? = null): Single<T> {
    return bindToLifecycle(view)
        .doOnSubscribe {
            manageSingleRx3Disposables(it, uniqueId)
        }.doOnDispose {
            removeSingleRx3Disposable(uniqueId)
        }.doOnTerminate {
            removeSingleRx3Disposable(uniqueId)
        }.allowEmpty()
}

fun <T : Any> Single<T>.allowEmpty(): Single<T> {
    return onErrorResumeNext {
        when (it is CancellationException) {
            true -> Single.never()
            false -> Single.error<T>(it)
        }
    }
}

private val singleDisposables = ConcurrentHashMap<String, Disposable>()

@PublishedApi
internal fun manageSingleRx3Disposables(disposable: Disposable, uniqueId: String?) {
    uniqueId?.let {
        singleDisposables[it]?.dispose()
        singleDisposables.put(it, disposable)
    }
}

@PublishedApi
internal fun removeSingleRx3Disposable(uniqueId: String?) {
    uniqueId?.let { singleDisposables.remove(it)?.dispose() }
}