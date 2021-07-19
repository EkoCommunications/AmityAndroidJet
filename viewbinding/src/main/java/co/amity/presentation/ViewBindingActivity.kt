package co.amity.presentation

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity

abstract class ViewBindingActivity<BINDING : ViewBinding> : RxAppCompatActivity() {

    private lateinit var vb: BINDING

    var binding: BINDING
        get() = vb
        private set(value) {
            vb = value
        }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = generateViewBinding()
        setContentView(vb.root)
    }

    abstract fun generateViewBinding(): BINDING
}