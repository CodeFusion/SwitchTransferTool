package ca.codefusion.switchtransfertool.ui.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.codefusion.switchtransfertool.SwitchTransferToolApplication
import ca.codefusion.switchtransfertool.ui.core.event.Event
import kotlinx.coroutines.launch

/**
 * Placeholder ViewModel class for future behavior (like error state)
 *
 * Use application context with care
 */
abstract class BaseViewModel(
    application: Application
) : AndroidViewModel(application) {

    /**
     * Called when this viewModel was bind to a view layout
     */
    open fun onViewBind() {}

    val application: SwitchTransferToolApplication by lazy { getApplication<SwitchTransferToolApplication>() }

    val isLoading = MutableLiveData(false)

    val showErrorToastEvent = MutableLiveData<Event<String>>()

    /**
     * Use this to automatically manage load state and catch server errors
     *
     * @param asyncCall block with the async function call
     * @param onKnownError Block is called when the [asyncCall] failed. It passes the original throwable as parameter and also
     * [ServerError]. It expects implementation to give further feedback on the error. If not provided, default user feedback is made
     * @param onUnknownError If provided, block is called when the [asyncCall] failed.  It expects implementation to give further feedback on the error. If not provided, default user feedback is made
     * @param loadingStatusHolder Optional variable to hold a specific load state only for this call. Defaults to using ViewModel's shared [isLoading]
     */
    fun <T> asyncExecute(
        asyncCall: suspend () -> T,
        loadingStatusHolder: MutableLiveData<Boolean>? = null,
        onComplete: ((result: T) -> Unit)? = null
    ) {
        loadingStatusHolder?.let {
            it.value = true
        } ?: run {
            isLoading.value = true
        }

        viewModelScope.launch {
            var result: T? = null
            try {
                result = asyncCall()
            } catch (t: Throwable) {
                showErrorToastEvent.value = Event(t.localizedMessage)
            }

            loadingStatusHolder?.let {
                it.value = false
            } ?: run {
                isLoading.value = false
            }

            result?.let { onComplete?.invoke(it) }
        }

    }
}