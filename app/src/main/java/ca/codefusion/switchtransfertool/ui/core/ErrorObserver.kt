package ca.codefusion.switchtransfertool.ui.core

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import ca.codefusion.switchtransfertool.ui.core.event.EventObserver

object ErrorObserver {

    fun observeVMErrors(fragment: Fragment, viewModel: BaseViewModel) {
        fragment.context?.let {
            observeVMErrors(it, fragment, viewModel)
        }
    }

    fun observeVMErrors(activity: AppCompatActivity, viewModel: BaseViewModel) {
        observeVMErrors(activity, activity, viewModel)
    }

    private fun observeVMErrors(context: Context, lifecycleOwner: LifecycleOwner, viewModel: BaseViewModel) {
        viewModel.showErrorToastEvent.observe(lifecycleOwner, EventObserver { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        })
    }
}