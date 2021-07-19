package co.amity.rxlifecycle

import android.view.View
import androidx.annotation.UiThread
import com.trello.rxlifecycle3.LifecycleProvider
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.android.FragmentEvent
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import com.trello.rxlifecycle3.kotlin.bindUntilEvent
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
inline fun <reified E, T> Single<T>.untilLifecycleEnd(lifecycleProvider: LifecycleProvider<E>, uniqueId: String? = null): Single<T> {
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
        manageSingleDisposables(it, uniqueId)
    }.doOnDispose {
        removeSingleDisposable(uniqueId)
    }.doOnTerminate {
        removeSingleDisposable(uniqueId)
    }.allowEmpty()
}

@UiThread
fun <T> Single<T>.untilLifecycleEnd(view: View, uniqueId: String? = null): Single<T> {
    return bindToLifecycle(view)
        .doOnSubscribe {
            manageSingleDisposables(it, uniqueId)
        }.doOnDispose {
            removeSingleDisposable(uniqueId)
        }.doOnTerminate {
            removeSingleDisposable(uniqueId)
        }.allowEmpty()
}

fun <T> Single<T>.allowEmpty(): Single<T> {
    return onErrorResumeNext {
        when (it is CancellationException) {
            true -> Single.never()
            false -> Single.error<T>(it)
        }
    }
}

private val singleDisposables = ConcurrentHashMap<String, Disposable>()

@PublishedApi
internal fun manageSingleDisposables(disposable: Disposable, uniqueId: String?) {
    uniqueId?.let {
        singleDisposables[it]?.dispose()
        singleDisposables.put(it, disposable)
    }
}

@PublishedApi
internal fun removeSingleDisposable(uniqueId: String?) {
    uniqueId?.let { singleDisposables.remove(it)?.dispose() }
}