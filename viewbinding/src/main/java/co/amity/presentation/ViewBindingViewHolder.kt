package co.amity.presentation

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ViewBindingViewHolder<ENTITY : Any, BINDING : ViewBinding>(context: Context, @LayoutRes val resource: Int) :
    RecyclerView.ViewHolder(View.inflate(context, resource, null)) {

    private var vb: BINDING

    var binding: BINDING
        get() = vb
        private set(value) {
            vb = value
        }

    init {
        vb = generateViewBinding()
    }

    abstract fun generateViewBinding(): BINDING

    open fun onBind(item: ENTITY) {

    }
}