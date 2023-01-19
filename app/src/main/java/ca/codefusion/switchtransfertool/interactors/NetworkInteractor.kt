package ca.codefusion.switchtransfertool.interactors

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.net.wifi.WifiNetworkSpecifier
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.preference.PreferenceManager
import ca.codefusion.switchtransfertool.data.models.MediaData
import ca.codefusion.switchtransfertool.helpers.PrefsNames
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NetworkInteractor() {

    fun requestNetwork(
        context: Context,
        ssid: String,
        pass: String,
        callback: NetworkCallback
    ) {
        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pass)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.requestNetwork(request, callback)
    }

    fun getData(wifiNetwork: Network): MediaData {
        val url = URL("http://192.168.0.1/data.json")
        val connection : HttpURLConnection = wifiNetwork.openConnection(url) as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 3000
        connection.connect()
        val status = connection.responseCode

        if (status == 200) {
            val br = BufferedReader(InputStreamReader(connection.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line + "\n")
            }
            br.close()
            return Gson().fromJson(sb.toString(), MediaData::class.java)
        } else {
            val exception = Exception("MediaData request failed")
            FirebaseCrashlytics.getInstance().recordException(exception)
            throw exception
        }
    }

    fun downloadMediaFiles(application: Application, files: List<String>, type: MediaData.FileType, wifiNetwork: Network) {
        downloadMediaScoped(application, files, type, wifiNetwork)
    }

    private fun downloadMediaScoped(application: Application, files: List<String>, type: MediaData.FileType, wifiNetwork: Network): Boolean {
        val resolver = application.contentResolver
        try {
            when (type) {
                MediaData.FileType.PHOTO -> {
                    val photoCollection =
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    files.forEach { file ->
                        val url = URL("http://192.168.0.1/img/$file")
                        val connection: HttpURLConnection =
                            wifiNetwork.openConnection(url) as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connect()
                        val imageInputStream: InputStream = connection.inputStream

                        val photoDetails = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, file)
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, getAppPhotoPath())
                        }

                        val photoUri = resolver.insert(photoCollection, photoDetails)

                        if (photoUri != null) {
                            resolver.openFileDescriptor(photoUri, "w").use { parcelFileDescriptor ->
                                ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor)
                                    .write(imageInputStream.readBytes())
                            }

                            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(application)
                            val useCaptureDate = sharedPrefs.getBoolean(PrefsNames.USE_CAPTURE_DATE, false)

                            val dateString = file.substring(0,14)
                            val dateTaken = if (useCaptureDate) {
                                LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                            } else {
                                LocalDateTime.now()
                            }

                            photoDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                            photoDetails.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken.atZone(ZoneId.systemDefault()).toInstant().epochSecond)
                            resolver.update(photoUri, photoDetails, null, null)
                        }
                    }
                    return true
                }
                MediaData.FileType.VIDEO -> {
                    val videoCollection =
                        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    files.forEach { file ->
                        val url = URL("http://192.168.0.1/img/$file")
                        val connection: HttpURLConnection =
                            wifiNetwork.openConnection(url) as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connect()
                        val videoInputStream: InputStream = connection.inputStream

                        val videoDetails = ContentValues().apply {
                            put(MediaStore.Video.Media.DISPLAY_NAME, file)
                            put(MediaStore.Video.Media.IS_PENDING, 1)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, getAppVideoPath())
                        }

                        val videoUri = resolver.insert(videoCollection, videoDetails)

                        if (videoUri != null) {
                            resolver.openFileDescriptor(videoUri, "w").use { parcelFileDescriptor ->
                                ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor)
                                    .write(videoInputStream.readBytes())
                            }

                            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(application)
                            val useCaptureDate = sharedPrefs.getBoolean(PrefsNames.USE_CAPTURE_DATE, false)

                            val dateString = file.substring(0,14)
                            val dateTaken = if (useCaptureDate) {
                                LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                            } else {
                                LocalDateTime.now()
                            }

                            videoDetails.put(MediaStore.Video.Media.IS_PENDING, 0)
                            videoDetails.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken.atZone(ZoneId.systemDefault()).toInstant().epochSecond)
                            resolver.update(videoUri, videoDetails, null, null)
                        }
                    }
                    return true
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            return false
        }
    }

    fun disconnectFromNetwork(context: Context, callback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(callback)
    }

    companion object {
        val APP_FOLDER_NAME = "SwitchTransferTool"

        fun getAppPhotoPath(): String {
            return "${Environment.DIRECTORY_PICTURES}/$APP_FOLDER_NAME"
        }

        fun getAppVideoPath(): String {
            return "${Environment.DIRECTORY_MOVIES}/$APP_FOLDER_NAME"
        }
    }
}