package ca.codefusion.switchtransfertool.ui.core

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import ca.codefusion.switchtransfertool.BR
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.reflect.KClass

/**
 * Base Activity to enable fast creation of new activities.
 *
 * Simply extend `BaseActivity` with associated ViewModel, Binding and resourceId.
 *
 * # Sample
 * MyActivity: BaseActivity<MyViewModel, MyActivityBinding> : {
 *      override val viewModelClass = MyViewModel::class
 *      override val layoutResId = R.layout.my_activity
 * }
 *
 */
abstract class BaseActivity<VM : BaseViewModel, VB : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var viewModel: VM
    protected abstract val viewModelClass: KClass<VM>

    protected abstract val layoutResId: Int
    protected lateinit var binding: VB

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = provideViewModel()
        binding = DataBindingUtil.setContentView(this, layoutResId)
        binding.lifecycleOwner = this
        binding.setVariable(BR.viewModel, viewModel)
        viewModel.onViewBind()

        ErrorObserver.observeVMErrors(this, viewModel)
    }

    open fun provideViewModel(): VM = getViewModel(viewModelClass)
}