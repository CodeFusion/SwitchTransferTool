package ca.codefusion.switchtransfertool.ui.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.reflect.KClass
import ca.codefusion.switchtransfertool.BR

/**
 * Base Fragment to enable fast creation of new fragments.
 *
 * Simply extend `BaseFragment` with associated ViewModel, Binding and resourceId.
 *
 * # Sample
 * MyFragment: BaseFragment<MyViewModel, MyFragmentBinding> : {
 *      override val viewModelClass = MyViewModel::class
 *      override val layoutResId = R.layout.my_fragment
 * }
 *
 */
abstract class BaseFragment<VM : BaseViewModel, VB : ViewDataBinding> : Fragment() {

    protected lateinit var viewModel: VM
    protected abstract val viewModelClass: KClass<VM>

    protected abstract val layoutResId: Int
    protected lateinit var binding: VB

    protected open val title: String? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = provideViewModel()
    }

    /**
     * open provider so that viewModels can be created in custom ways
     *
     * E.G. sharing viewModels in activity:
     * val viewModel by activityViewModels<MyViewModel>()
     */
    open fun provideViewModel(): VM {
        return getViewModel(viewModelClass)
    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutResId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.setVariable(BR.viewModel, viewModel)
        viewModel.onViewBind()
        binding.executePendingBindings()

        onAfterCreateView(savedInstanceState)

        return binding.root
    }

    /**
     * Can be overridden to use savedInstanceState after binding is setup with viewModel
     */
    open fun onAfterCreateView(savedInstanceState: Bundle?) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title?.let {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it
        }

        ErrorObserver.observeVMErrors(this, viewModel)
    }
}