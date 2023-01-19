package ca.codefusion.switchtransfertool.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import ca.codefusion.switchtransfertool.data.localstorage.LocalRepository

class TutorialUtil(
    private val localRepository: LocalRepository,
    private val context: Context
) {
    fun shouldShowTutorial(connectionType: ConnectionType): Boolean {
        return when (connectionType) {
            ConnectionType.SCANNER -> {
                localRepository.getShowQRTutorial() || !hasPermission(Manifest.permission.CAMERA)
            }
            ConnectionType.MANUAL -> {
                localRepository.getShowManualTutorial()
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }
}