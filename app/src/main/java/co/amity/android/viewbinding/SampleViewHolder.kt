package co.amity.android.viewbinding

import android.content.Context
import android.view.View
import co.amity.android.R
import co.amity.android.databinding.ViewHolderSampleBinding
import co.amity.presentation.ViewBindingViewHolder

class SampleViewHolder(context: Context) : ViewBindingViewHolder<Any, ViewHolderSampleBinding>(View.inflate(context, R.layout.view_holder_sample, null)) {

    override fun generateViewBinding(): ViewHolderSampleBinding {
        return ViewHolderSampleBinding.bind(itemView)
    }

    override fun onBind(item: Any) {
        binding.viewHolderTextView.text = "hello world!"

        binding.viewHolderSampleButton.setOnClickListener {

        }
    }
}