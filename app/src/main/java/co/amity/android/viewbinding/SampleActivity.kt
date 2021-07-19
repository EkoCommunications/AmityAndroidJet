package co.amity.android.viewbinding

import android.os.Bundle
import co.amity.android.databinding.ActivitySampleBinding
import co.amity.presentation.ViewBindingActivity

class SampleActivity : ViewBindingActivity<ActivitySampleBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.activitySampleTextView.text = "hello world!"

        viewBinding.activitySampleButton.setOnClickListener {

        }
    }

    override fun generateViewBinding(): ActivitySampleBinding {
        return ActivitySampleBinding.inflate(layoutInflater)
    }
}