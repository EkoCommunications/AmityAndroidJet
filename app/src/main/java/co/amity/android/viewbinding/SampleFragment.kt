package co.amity.android.viewbinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.amity.android.databinding.FragmentSampleBinding
import co.amity.presentation.ViewBindingFragment

class SampleFragment : ViewBindingFragment<FragmentSampleBinding>() {

    override fun generateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSampleBinding {
        return FragmentSampleBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentSampleTextView.text = "hello world!"

        binding.fragmentSampleButton.setOnClickListener {

        }
    }
}