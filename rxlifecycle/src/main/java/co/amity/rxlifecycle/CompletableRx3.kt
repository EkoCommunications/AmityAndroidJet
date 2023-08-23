package co.amity.rxlifecycle

import android.view.View
import androidx.annotation.UiThread
import com.trello.rxlifecycle4.LifecycleProvider
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
inline fun <reified E> Completable.untilLifecycleEnd(
    lifecycleProvider: LifecycleProvider<E>,
    uniqueId: String? = null
): Completable {
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
        manageCompletableRx3Disposables(it, uniqueId)
    }.doOnDispose {
        removeCompletableRx3Disposable(uniqueId)
    }.doOnTerminate {
        removeCompletableRx3Disposable(uniqueId)
    }.allowInComplete()
}

@UiThread
fun Completable.untilLifecycleEnd(view: View, uniqueId: String? = null): Completable {
    return bindToLifecycle(view)
        .doOnSubscribe {
            manageCompletableRx3Disposables(it, uniqueId)
        }.doOnDispose {
            removeCompletableRx3Disposable(uniqueId)
        }.doOnTerminate {
            removeCompletableRx3Disposable(uniqueId)
        }.allowInComplete()
}

fun Completable.allowInComplete(): Completable {
    return onErrorComplete {
        it is CancellationException
    }
}

private val completableDisposables = ConcurrentHashMap<String, Disposable>()

@PublishedApi
internal fun manageCompletableRx3Disposables(disposable: Disposable, uniqueId: String?) {
    uniqueId?.let {
        completableDisposables[it]?.dispose()
        completableDisposables.put(it, disposable)
    }
}

@PublishedApi
internal fun removeCompletableRx3Disposable(uniqueId: String?) {
    uniqueId?.let { completableDisposables.remove(it)?.dispose() }
}