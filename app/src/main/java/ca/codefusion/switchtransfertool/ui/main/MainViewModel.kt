package ca.codefusion.switchtransfertool.ui.main

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import ca.codefusion.switchtransfertool.data.localstorage.LocalRepository
import ca.codefusion.switchtransfertool.data.models.MediaFile
import ca.codefusion.switchtransfertool.helpers.PrefsNames
import ca.codefusion.switchtransfertool.interactors.GalleryInteractor
import ca.codefusion.switchtransfertool.interactors.SettingsInteractor
import ca.codefusion.switchtransfertool.ui.core.BaseViewModel
import ca.codefusion.switchtransfertool.utils.ConnectionType
import ca.codefusion.switchtransfertool.utils.Event
import ca.codefusion.switchtransfertool.utils.SingleLiveEvent
import ca.codefusion.switchtransfertool.utils.TutorialUtil
import com.chillibits.simplesettings.tool.getPrefStringValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val galleryInteractor: GalleryInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val localRepository: LocalRepository,
    val tutorialUtil: TutorialUtil
) : BaseViewModel(application) {

    private val _transferButtonEvent = SingleLiveEvent<Unit>()
    val transferButtonEvent: LiveData<Unit> = _transferButtonEvent

    private val _galleryItems = MutableLiveData<List<MediaFile>>()
    val galleryItems: LiveData<List<MediaFile>> = _galleryItems

    private val _showPreview = MutableLiveData<Boolean>(false)
    val showPreview: LiveData<Boolean> = _showPreview

    private val _previewItem = MutableLiveData<MediaFile>()
    val previewItem: LiveData<MediaFile> = _previewItem

    private val _openExternalClicked = MutableLiveData<Event<MediaFile>>()
    val openExternalClicked: LiveData<Event<MediaFile>> = _openExternalClicked

    private val _deleteClicked = MutableLiveData<Event<MediaFile>>()
    val deleteClicked: LiveData<Event<MediaFile>> = _deleteClicked

    private val _shareClicked = MutableLiveData<Event<MediaFile>>()
    val shareClicked: LiveData<Event<MediaFile>> = _shareClicked

    private val _settingsClicked = SingleLiveEvent<Unit>()
    val settingsClicked: LiveData<Unit> = _settingsClicked

    fun transferButtonPressed() {
        _transferButtonEvent.value = Unit
    }

    fun generateGallery(context: Context) {
        viewModelScope.launch {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            var mediaList = galleryInteractor.getImages(context) + galleryInteractor.getVideos(context)
            mediaList = if (context.getPrefStringValue(PrefsNames.GALLERY_SORT_ORDER, "0") == "0") {
                mediaList.sortedByDescending {
                    when (sharedPrefs.getBoolean(PrefsNames.USE_CAPTURE_DATE, true)) {
                        true -> it.dateTaken
                        false -> it.dateAdded
                }}
            } else {
                mediaList.sortedBy {
                    when (sharedPrefs.getBoolean(PrefsNames.USE_CAPTURE_DATE, true)) {
                        true -> it.dateTaken
                        false -> it.dateAdded
                }}
            }
            _galleryItems.value = getGalleryItemViewModels(mediaList)
        }
    }

    private fun getGalleryItemViewModels(
        galleryItems: List<MediaFile>
    ): List<MediaFile> {
        val galleryItemViewModels = mutableListOf<MediaFile>()
        galleryItems.forEach { mediaFile ->
            galleryItemViewModels.add(
                    mediaFile
            )
        }
        return galleryItemViewModels
    }

    fun showPreview(mediaFile: MediaFile) {
        _previewItem.value = mediaFile
        _showPreview.value = true
    }

    fun openExternalClicked() {
        previewItem.value?.let {
            _openExternalClicked.value = Event(it)
        }
    }

    fun deleteClicked() {
        previewItem.value?.let {
            _deleteClicked.value = Event(it)
        }
    }

    fun shareClicked() {
        previewItem.value?.let {
            _shareClicked.value = Event(it)
        }
    }

    fun backPressed() {
        Log.d("Back", "closePreview")
        _showPreview.value = false
    }

    fun settingsClicked() {
        _settingsClicked.value = Unit
    }

    fun deletePreviewFile(context: Context, activity: Activity) {
        previewItem.value?.let {
            viewModelScope.launch(Dispatchers.IO) {
                galleryInteractor.deleteFile(it, context, activity)
                viewModelScope.launch(Dispatchers.Main) {
                    _showPreview.value = false
                }
            }
        }
    }

    fun navigateToScanner(fragment: Fragment) {
        val manualMode = localRepository.isDefaultManualMode()
        val action = if (manualMode) {
            if (tutorialUtil.shouldShowTutorial(ConnectionType.MANUAL)) {
                MainFragmentDirections.actionMainFragmentToTutorialFragment(ConnectionType.MANUAL)
            } else {
                MainFragmentDirections.actionMainFragmentToDownloadingFragment(ConnectionType.MANUAL)
            }
        } else {
            if (tutorialUtil.shouldShowTutorial(ConnectionType.SCANNER)) {
                MainFragmentDirections.actionMainFragmentToTutorialFragment(ConnectionType.SCANNER)
            } else {
                MainFragmentDirections.actionMainFragmentToScannerFragment()
            }
        }
        fragment.findNavController().navigate(action)
    }

    fun showSettings(context: Context) {
        settingsInteractor.showSettings(context)
    }

    sealed class GalleryItemViewModel {

        data class ImageItemViewModel(
            val imageUri: Uri,
            val contentDescription: String
        ) : GalleryItemViewModel()
    }
}