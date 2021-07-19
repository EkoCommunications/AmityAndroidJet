package co.amity.rxlifecycle

import android.view.View
import com.trello.rxlifecycle3.LifecycleProvider
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.android.FragmentEvent
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import com.trello.rxlifecycle3.kotlin.bindUntilEvent
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
inline fun <reified E> Completable.untilLifecycleEnd(lifecycleProvider: LifecycleProvider<E>, uniqueId: String? = null): Completable {
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
        manageCompletableDisposables(it, uniqueId)
    }.doOnDispose {
        removeCompletableDisposable(uniqueId)
    }.doOnTerminate {
        removeCompletableDisposable(uniqueId)
    }.allowInComplete()
}

fun Completable.untilLifecycleEnd(view: View, uniqueId: String? = null): Completable {
    return bindToLifecycle(view)
        .doOnSubscribe {
            manageCompletableDisposables(it, uniqueId)
        }.doOnDispose {
            removeCompletableDisposable(uniqueId)
        }.doOnTerminate {
            removeCompletableDisposable(uniqueId)
        }.allowInComplete()
}

fun Completable.allowInComplete(): Completable {
    return onErrorComplete {
        it is CancellationException
    }
}

private val completableDisposables = ConcurrentHashMap<String, Disposable>()

@PublishedApi
internal fun manageCompletableDisposables(disposable: Disposable, uniqueId: String?) {
    uniqueId?.let {
        completableDisposables[it]?.dispose()
        completableDisposables.put(it, disposable)
    }
}

@PublishedApi
internal fun removeCompletableDisposable(uniqueId: String?) {
    uniqueId?.let { completableDisposables.remove(it)?.dispose() }
}