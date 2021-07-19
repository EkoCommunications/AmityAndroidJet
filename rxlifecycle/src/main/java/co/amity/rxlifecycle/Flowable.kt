package co.amity.rxlifecycle

import android.view.View
import androidx.annotation.UiThread
import com.trello.rxlifecycle3.LifecycleProvider
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.android.FragmentEvent
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import com.trello.rxlifecycle3.kotlin.bindUntilEvent
import io.reactivex.Flowable
import org.reactivestreams.Subscription
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
inline fun <reified E, T> Flowable<T>.untilLifecycleEnd(lifecycleProvider: LifecycleProvider<E>, uniqueId: String? = null): Flowable<T> {
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
        manageFlowableSubscriptions(it, uniqueId)
    }.doOnCancel {
        removeFlowableSubscription(uniqueId)
    }.doOnTerminate {
        removeFlowableSubscription(uniqueId)
    }
}

@UiThread
fun <T> Flowable<T>.untilLifecycleEnd(view: View, uniqueId: String? = null): Flowable<T> {
    return bindToLifecycle(view)
        .doOnSubscribe {
            manageFlowableSubscriptions(it, uniqueId)
        }.doOnCancel {
            removeFlowableSubscription(uniqueId)
        }.doOnTerminate {
            removeFlowableSubscription(uniqueId)
        }
}

private val flowableSubscriptions = ConcurrentHashMap<String, Subscription>()

@PublishedApi
internal fun manageFlowableSubscriptions(subscription: Subscription, uniqueId: String?) {
    uniqueId?.let {
        flowableSubscriptions[it]?.cancel()
        flowableSubscriptions.put(it, subscription)
    }
}

@PublishedApi
internal fun removeFlowableSubscription(uniqueId: String?) {
    uniqueId?.let { flowableSubscriptions.remove(it)?.cancel() }
}