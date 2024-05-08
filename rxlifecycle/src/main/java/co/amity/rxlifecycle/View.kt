package co.amity.rxlifecycle

import android.view.View
import com.trello.rxlifecycle3.LifecycleProvider
import com.trello.rxlifecycle3.LifecycleTransformer
import com.trello.rxlifecycle3.RxLifecycle
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

fun View.lifecycleProviderFromView(): LifecycleProvider<ViewEvent> {
    val subject = BehaviorSubject.create<ViewEvent>()

    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {
            subject.onNext(ViewEvent.ATTACH)
        }

        override fun onViewDetachedFromWindow(view: View) {
            subject.onNext(ViewEvent.DETACH)
        }
    })

    return object : LifecycleProvider<ViewEvent> {
        override fun lifecycle(): Observable<ViewEvent> {
            return subject.hide()
        }

        override fun <T : Any?> bindUntilEvent(event: ViewEvent): LifecycleTransformer<T> {
            return RxLifecycle.bindUntilEvent(lifecycle(), event)
        }

        override fun <T : Any?> bindToLifecycle(): LifecycleTransformer<T> {
            return bindUntilEvent(ViewEvent.DETACH)
        }
    }
}

fun View.lifecycleProviderFromViewRx3(): LifecycleProvider<ViewEvent> {
    val subject = BehaviorSubject.create<ViewEvent>()

    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {
            subject.onNext(ViewEvent.ATTACH)
        }

        override fun onViewDetachedFromWindow(view: View) {
            subject.onNext(ViewEvent.DETACH)
        }
    })

    return object : LifecycleProvider<ViewEvent> {
        override fun lifecycle(): Observable<ViewEvent> {
            return subject.hide()
        }

        override fun <T : Any?> bindUntilEvent(event: ViewEvent): LifecycleTransformer<T> {
            return RxLifecycle.bindUntilEvent(lifecycle(), event)
        }

        override fun <T : Any?> bindToLifecycle(): LifecycleTransformer<T> {
            return bindUntilEvent(ViewEvent.DETACH)
        }
    }
}