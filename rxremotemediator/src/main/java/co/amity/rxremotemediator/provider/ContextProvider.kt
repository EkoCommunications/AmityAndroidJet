package co.amity.rxremotemediator.provider

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.Subject

class ContextProvider : BaseProvider() {

    override fun onCreate(): Boolean {
        subject.onNext(requireContext())
        subject.onComplete()
        return false
    }

    companion object {
        private val subject: Subject<Context> = AsyncSubject.create()
        val context: Flowable<Context>
            get() = subject.toFlowable(BackpressureStrategy.BUFFER)
    }
}
