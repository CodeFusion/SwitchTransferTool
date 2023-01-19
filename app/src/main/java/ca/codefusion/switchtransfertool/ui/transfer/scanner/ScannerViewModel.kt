package ca.codefusion.switchtransfertool.ui.transfer.scanner

import android.app.Application
import androidx.lifecycle.LiveData
import ca.codefusion.switchtransfertool.data.localstorage.LocalRepository
import ca.codefusion.switchtransfertool.ui.core.BaseViewModel
import ca.codefusion.switchtransfertool.utils.SingleLiveEvent

class ScannerViewModel(
    application: Application,
    private val localRepository: LocalRepository
) : BaseViewModel(application) {

    private val _backButtonPressed = SingleLiveEvent<Unit>()
    val backButtonPressed: LiveData<Unit> = _backButtonPressed

    private val _showTutorial = SingleLiveEvent<Unit>()
    val showTutorial: LiveData<Unit> = _showTutorial

    private val _manualButtonEvent = SingleLiveEvent<Unit>()
    val manualButtonEvent: LiveData<Unit> = _manualButtonEvent

    fun onHelpPressed() {
        _showTutorial.value = Unit
    }

    fun onBackButtonPressed() {
        _backButtonPressed.value = Unit
    }

    fun onManualButtonPressed() {
        _manualButtonEvent.value = Unit
    }
}