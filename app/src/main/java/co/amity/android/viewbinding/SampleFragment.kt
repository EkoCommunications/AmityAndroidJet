package co.amity.android.viewbinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import co.amity.android.databinding.FragmentSampleBinding
import co.amity.presentation.ViewBindingFragment

class SampleFragment : ViewBindingFragment<FragmentSampleBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.fragmentSampleTextView.text = "hello world!"

        viewBinding.fragmentSampleButton.setOnClickListener {

        }
    }

    override fun generateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSampleBinding {
        return FragmentSampleBinding.inflate(inflater, container, false)
    }
}