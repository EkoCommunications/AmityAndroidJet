package co.amity.rxlifecycle

import android.view.View
import androidx.annotation.UiThread
import com.trello.rxlifecycle4.LifecycleProvider
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
inline fun <reified E, T> Maybe<T>.untilLifecycleEnd(
    lifecycleProvider: LifecycleProvider<E>,
    uniqueId: String? = null
): Maybe<T> {
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
        manageMaybeRx3Disposables(it, uniqueId)
    }.doOnDispose {
        removeMaybeRx3Disposable(uniqueId)
    }.doOnTerminate {
        removeMaybeRx3Disposable(uniqueId)
    }
}

@UiThread
fun <T> Maybe<T>.untilLifecycleEnd(view: View, uniqueId: String? = null): Maybe<T> {
    return bindToLifecycle(view)
        .doOnSubscribe {
            manageMaybeRx3Disposables(it, uniqueId)
        }.doOnDispose {
            removeMaybeRx3Disposable(uniqueId)
        }.doOnTerminate {
            removeMaybeRx3Disposable(uniqueId)
        }
}

private val maybeDisposables = ConcurrentHashMap<String, Disposable>()

@PublishedApi
internal fun manageMaybeRx3Disposables(disposable: Disposable, uniqueId: String?) {
    uniqueId?.let {
        maybeDisposables[it]?.dispose()
        maybeDisposables.put(it, disposable)
    }
}

@PublishedApi
internal fun removeMaybeRx3Disposable(uniqueId: String?) {
    uniqueId?.let { maybeDisposables.remove(it)?.dispose() }
}