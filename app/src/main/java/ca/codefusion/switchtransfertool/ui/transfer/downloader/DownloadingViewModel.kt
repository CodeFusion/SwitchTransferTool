package ca.codefusion.switchtransfertool.ui.transfer.downloader

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.codefusion.switchtransfertool.data.localstorage.LocalRepository
import ca.codefusion.switchtransfertool.data.models.MediaData
import ca.codefusion.switchtransfertool.interactors.NetworkInteractor
import ca.codefusion.switchtransfertool.ui.core.BaseViewModel
import ca.codefusion.switchtransfertool.utils.SingleLiveEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

class DownloadingViewModel(
    application: Application,
    private val networkInteractor: NetworkInteractor,
    private val localRepository: LocalRepository
) : BaseViewModel(application) {

    private val _galleryButtonEvent = SingleLiveEvent<Unit>()
    val galleryButtonEvent: LiveData<Unit> = _galleryButtonEvent

    private val _connectionFailureEvent = SingleLiveEvent<Unit>()
    val connectionFailureEvent: LiveData<Unit> = _connectionFailureEvent

    private val _moreInfoEvent = SingleLiveEvent<Unit>()
    val moreInfoEvent: LiveData<Unit> = _moreInfoEvent

    private val _state = MutableLiveData(CONNECTING)
    val state: LiveData<Int> = _state

    private val _imageCount = MutableLiveData<Int>()
    val imageCount: LiveData<Int> = _imageCount

    private val _showHelp = SingleLiveEvent<Unit>()
    val showHelp: LiveData<Unit> = _showHelp

    private val _showWifiSettings = SingleLiveEvent<Unit>()
    val showWifiSettings: LiveData<Unit> = _showWifiSettings

    private val _scanButtonEvent = SingleLiveEvent<Unit>()
    val scanButtonEvent: LiveData<Unit> = _scanButtonEvent

    private var wifiNetwork: Network? = null

    fun connectToNetwork(context: Context, ssid: String, pass: String) {
        networkInteractor.requestNetwork(context, ssid, pass, networkCallback)
    }

    fun startManualConnectionCheck() {
        if (connectedToSwitch()) {
            _state.value = DOWNLOADING
            downloadFiles()
        } else {
            monitorSSID()
        }
    }

    fun checkManualConnection() {
        if (connectedToSwitch()) {
            _state.value = DOWNLOADING
            downloadFiles()
        }
    }

    private fun monitorSSID() {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        _state.value = AWAITING_SSID
    }

    fun downloadFiles() {
        viewModelScope.launch(Dispatchers.IO) {

            // This is stupid, but it works
            while (!connectedToSwitch()) {
                Timber.d("Network not stable, waiting 1000ms")
                delay(1000)
            }

            try {
                Timber.d("Fetching media list")
                val mediaData = networkInteractor.getData(wifiNetwork!!)
                Timber.d("Media list fetched. Downloading media files")
                networkInteractor.downloadMediaFiles(
                    application,
                    mediaData.fileNames,
                    mediaData.fileType,
                    wifiNetwork!!
                )
                viewModelScope.launch(Main) {
                    when {
                        mediaData.fileType == MediaData.FileType.VIDEO -> {
                            _state.value = SUCCESS_VIDEO
                        }
                        mediaData.fileNames.size > 1 -> {
                            _imageCount.value = mediaData.fileNames.size
                            _state.value = SUCCESS_MULTIPLE_IMAGES
                        }
                        else -> {
                            _state.value = SUCCESS_IMAGE
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.d(e.message, e.stackTraceToString())
                viewModelScope.launch(Main) {
                    _state.value = UNKNOWN_ERROR
                }
            }

        }
    }

    fun connectedToSwitch(): Boolean {
        return try {
            val url = URL("http://192.168.0.1/data.json")
            val connection: HttpURLConnection = wifiNetwork?.openConnection(url) as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.connect()
            val status = connection.responseCode
            status == 200
        } catch (e: Exception) {
            false
        }
    }

    fun disconnectFromNetwork(context: Context) {
        networkInteractor.disconnectFromNetwork(context, networkCallback)
    }

    fun galleryButtonClick() {
        _galleryButtonEvent.value = Unit
    }

    fun moreInfoClick() {
        _moreInfoEvent.value = Unit
    }

    fun onHelpClicked() {
        _showHelp.value = Unit
    }

    fun wifiSettingsClick() {
        _showWifiSettings.value = Unit
    }

    fun onScanClicked() {
        _scanButtonEvent.value = Unit
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onUnavailable() {
            viewModelScope.launch(Main) {
                _state.value = UNAVAILABLE
            }
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            Timber.d("OnLosing")
            super.onLosing(network, maxMsToLive)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onAvailable(network: Network) {
            super.onAvailable(network)

            wifiNetwork = network
            if (!connectedToSwitch()) {
                if (state.value == CONNECTING) {
                    viewModelScope.launch (Main) {
                        _state.value = UNAVAILABLE
                    }
                }
                return
            }

            val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            connectivityManager?.bindProcessToNetwork(network)
            viewModelScope.launch(Main) {
                _state.value = DOWNLOADING
            }
            downloadFiles()
        }

        override fun onLost(network: Network) {
            Timber.d("Network Lost")
            super.onLost(network)

            // Ignore lost connection if not connected to switch network yet
            if (_state.value != AWAITING_SSID) {
                viewModelScope.launch(Main) {
                    _state.value = LOST
                }
            }
        }
    }

    companion object STATE {
        const val AWAITING_SSID = -1
        const val CONNECTING = 0
        const val UNAVAILABLE = 1
        const val LOST = 2
        const val DOWNLOADING = 3
        const val SUCCESS_IMAGE = 4
        const val SUCCESS_MULTIPLE_IMAGES = 5
        const val SUCCESS_VIDEO = 6
        const val UNKNOWN_ERROR = 7
    }
}