package ca.codefusion.switchtransfertool.ui.transfer.scanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(val listener: ScanningResultListener) : ImageAnalysis.Analyzer {

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        // Look for WiFi QR Codes
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE
            ).build()
        val scanner = BarcodeScanning.getClient(options)

        mediaImage?.let { safeImage ->
            val image = InputImage.fromMediaImage(safeImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodeList ->
                    // Return the first detected barcode
                    for (barcode in barcodeList) {
                        val valueType = barcode.valueType
                        when (valueType) {
                            Barcode.TYPE_WIFI -> {
                                barcode.wifi?.let { wifi ->
                                    val ssid = wifi.ssid ?: ""
                                    val password = wifi.password ?: ""
                                    val type = wifi.encryptionType
                                    listener.onScanned(ssid, password, type)
                                }
                            }
                        }
                    }
                    imageProxy.close()
                }
                .addOnFailureListener { exception ->
                    FirebaseCrashlytics.getInstance().recordException(exception)
                    imageProxy.close()
                }
        }
    }

    interface ScanningResultListener {
        fun onScanned(ssid: String, password: String, type: Int)
    }
}