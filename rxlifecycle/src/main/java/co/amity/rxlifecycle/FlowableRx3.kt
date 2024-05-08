package co.amity.rxlifecycle

import android.view.View
import androidx.annotation.UiThread
import com.trello.rxlifecycle4.LifecycleProvider
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.core.Flowable
import org.reactivestreams.Subscription
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
inline fun <reified E, T : Any> Flowable<T>.untilLifecycleEnd(
    lifecycleProvider: LifecycleProvider<E>,
    uniqueId: String? = null
): Flowable<T> {
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
        manageFlowableRx3Subscriptions(it, uniqueId)
    }.doOnCancel {
        removeFlowableRx3Subscription(uniqueId)
    }.doOnTerminate {
        removeFlowableRx3Subscription(uniqueId)
    }
}

@UiThread
fun <T : Any> Flowable<T>.untilLifecycleEnd(view: View, uniqueId: String? = null): Flowable<T> {
    return bindToLifecycle(view)
        .doOnSubscribe {
            manageFlowableRx3Subscriptions(it, uniqueId)
        }.doOnCancel {
            removeFlowableRx3Subscription(uniqueId)
        }.doOnTerminate {
            removeFlowableRx3Subscription(uniqueId)
        }
}

private val flowableSubscriptions = ConcurrentHashMap<String, Subscription>()

@PublishedApi
internal fun manageFlowableRx3Subscriptions(subscription: Subscription, uniqueId: String?) {
    uniqueId?.let {
        flowableSubscriptions[it]?.cancel()
        flowableSubscriptions.put(it, subscription)
    }
}

@PublishedApi
internal fun removeFlowableRx3Subscription(uniqueId: String?) {
    uniqueId?.let { flowableSubscriptions.remove(it)?.cancel() }
}