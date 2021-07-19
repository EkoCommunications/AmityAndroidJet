package co.amity.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.trello.rxlifecycle3.components.support.RxFragment

abstract class ViewBindingFragment<VB : ViewBinding> : RxFragment() {

    private lateinit var vb: VB

    var binding: VB
        get() = vb
        private set(value) {
            vb = value
        }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vb = generateViewBinding(inflater, container)
        return vb.root
    }

    abstract fun generateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
}