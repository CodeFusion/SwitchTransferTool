package ca.codefusion.switchtransfertool.ui.transfer.tutorial

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import ca.codefusion.switchtransfertool.data.localstorage.LocalRepository
import ca.codefusion.switchtransfertool.ui.core.BaseViewModel
import ca.codefusion.switchtransfertool.ui.transfer.downloader.DownloadingFragment
import ca.codefusion.switchtransfertool.utils.ConnectionType
import ca.codefusion.switchtransfertool.utils.SingleLiveEvent

class TutorialViewModel(
    application: Application,
    private val localRepository: LocalRepository
) : BaseViewModel(application) {

    private val _skipButtonVisible = MutableLiveData<Boolean>(true)
    val skipButtonVisible: LiveData<Boolean> = _skipButtonVisible

    private val _nextButtonVisible = MutableLiveData<Boolean>(true)
    val nextButtonVisible: LiveData<Boolean> = _nextButtonVisible

    private val _nextButtonClick = SingleLiveEvent<Unit>()
    val nextButtonClick: LiveData<Unit> = _nextButtonClick

    private val _skipButtonClick = SingleLiveEvent<Unit>()
    val skipButtonClick: LiveData<Unit> = _skipButtonClick

    fun skipButtonVisible(visible: Boolean) {
        _skipButtonVisible.value = visible
    }

    fun nextButtonVisible(visible: Boolean) {
        _nextButtonVisible.value = visible
    }

    fun nextButtonClicked() {
        _nextButtonClick.value = Unit
    }

    fun skipButtonClicked() {
        _skipButtonClick.value = Unit
    }
}