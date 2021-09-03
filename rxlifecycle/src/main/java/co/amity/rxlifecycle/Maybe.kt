package co.amity.rxlifecycle

import android.view.View
import androidx.annotation.UiThread
import com.trello.rxlifecycle3.LifecycleProvider
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.android.FragmentEvent
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import com.trello.rxlifecycle3.kotlin.bindUntilEvent
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
inline fun <reified E, T> Maybe<T>.untilLifecycleEnd(lifecycleProvider: LifecycleProvider<E>, uniqueId: String? = null): Maybe<T> {
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
        manageMaybeDisposables(it, uniqueId)
    }.doOnDispose {
        removeMaybeDisposable(uniqueId)
    }.doOnTerminate {
        removeMaybeDisposable(uniqueId)
    }
}

@UiThread
fun <T> Maybe<T>.untilLifecycleEnd(view: View, uniqueId: String? = null): Maybe<T> {
    return bindToLifecycle(view)
        .doOnSubscribe {
            manageMaybeDisposables(it, uniqueId)
        }.doOnDispose {
            removeMaybeDisposable(uniqueId)
        }.doOnTerminate {
            removeMaybeDisposable(uniqueId)
        }
}

private val maybeDisposables = ConcurrentHashMap<String, Disposable>()

@PublishedApi
internal fun manageMaybeDisposables(disposable: Disposable, uniqueId: String?) {
    uniqueId?.let {
        maybeDisposables[it]?.dispose()
        maybeDisposables.put(it, disposable)
    }
}

@PublishedApi
internal fun removeMaybeDisposable(uniqueId: String?) {
    uniqueId?.let { maybeDisposables.remove(it)?.dispose() }
}