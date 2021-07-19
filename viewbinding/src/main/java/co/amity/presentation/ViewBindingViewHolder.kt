package co.amity.presentation

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ViewBindingViewHolder<ENTITY : Any, BINDING : ViewBinding>(itemView: View) : RecyclerView.ViewHolder(itemView) {

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