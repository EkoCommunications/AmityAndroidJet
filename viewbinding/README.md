# Amity View Binding

### Easily create a binding class for your Activity, Fragment and ViewHolder 

```code 
class SampleActivity : ViewBindingActivity<ActivitySampleBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.activitySampleTextView.text = "hello world!"

        binding.activitySampleButton.setOnClickListener {

        }
    }

    override fun generateViewBinding(): ActivitySampleBinding {
        return ActivitySampleBinding.inflate(layoutInflater)
    }
}
```

```code 
class SampleFragment : ViewBindingFragment<FragmentSampleBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.fragmentSampleTextView.text = "hello world!"

        binding.fragmentSampleButton.setOnClickListener {

        }
    }

    override fun generateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSampleBinding {
        return FragmentSampleBinding.inflate(inflater, container, false)
    }
}
```

```code 
class SampleViewHolder(context: Context) : ViewBindingViewHolder<Any, ViewHolderSampleBinding>(context, R.layout.view_holder_sample) {

    override fun generateViewBinding(): ViewHolderSampleBinding {
        return ViewHolderSampleBinding.bind(itemView)
    }

    override fun onBind(item: Any) {
        binding.viewHolderTextView.text = "hello world!"

        binding.viewHolderSampleButton.setOnClickListener {

        }
    }
}
```

ref: https://developer.android.com/topic/libraries/view-binding
