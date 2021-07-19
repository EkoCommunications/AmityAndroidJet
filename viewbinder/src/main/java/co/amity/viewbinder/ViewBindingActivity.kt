package co.amity.viewbinder

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity

abstract class ViewBindingActivity<VB : ViewBinding> : RxAppCompatActivity() {

    private lateinit var vb: VB

    var viewBinding: VB
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

    abstract fun generateViewBinding(): VB
}