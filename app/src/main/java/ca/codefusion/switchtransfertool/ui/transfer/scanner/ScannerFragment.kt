package ca.codefusion.switchtransfertool.ui.transfer.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ScaleGestureDetectorCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import ca.codefusion.switchtransfertool.R
import ca.codefusion.switchtransfertool.databinding.ScannerFragmentBinding
import ca.codefusion.switchtransfertool.ui.core.BaseFragment
import ca.codefusion.switchtransfertool.utils.ConnectionType
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ScannerFragment : BaseFragment<ScannerViewModel, ScannerFragmentBinding>() {

    override val viewModelClass = ScannerViewModel::class
    override val layoutResId = R.layout.scanner_fragment

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String?>

    private var toast: Toast? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {
                    startCamera()
                } else {
                    // feature unavailable, fall back to manual entry
                }
            }

        cameraExecutor = Executors.newSingleThreadExecutor()

        handlePermissions()

        viewModel.backButtonPressed.observe(viewLifecycleOwner, Observer {
            requireActivity().onBackPressed()
        })

        viewModel.showTutorial.observe(viewLifecycleOwner) {
            val action =
                ScannerFragmentDirections.actionScannerFragmentToTutorialFragment(ConnectionType.SCANNER)
            findNavController().navigate(action)
        }

        viewModel.manualButtonEvent.observe(viewLifecycleOwner) {
            val action = ScannerFragmentDirections.actionScannerFragmentToConnectingFragment(ConnectionType.MANUAL)
            findNavController().navigate(action)
        }

        val scaleGestureDetector = ScaleGestureDetector(requireContext(),
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (this@ScannerFragment::camera.isInitialized) {
                    val scale = camera.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                    camera.cameraControl.setZoomRatio(scale)
                    return true
                }
                return false
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                super.onScaleEnd(detector)
                if (this@ScannerFragment::camera.isInitialized) {
                    val autoFocusAction =  FocusMeteringAction.Builder(
                        SurfaceOrientedMeteringPointFactory(1f, 1f).createPoint(.5f, .5f),
                        FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                    ).apply { setAutoCancelDuration(2, TimeUnit.SECONDS) }
                        .build()
                    camera.cameraControl.startFocusAndMetering(autoFocusAction)
                }
            }
        })

        binding.viewfinder.setOnTouchListener { view, event ->
            view.performClick()
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    private fun handlePermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // show educational UI
            }
            else -> {
                // Ask for permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindPreview()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindPreview() {
        if (requireActivity().isDestroyed || requireActivity().isFinishing) {
            return
        }

        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.viewfinder.surfaceProvider)
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        class ScanningListener : BarcodeAnalyzer.ScanningResultListener {
            override fun onScanned(ssid: String, password: String, type: Int) {
                requireActivity().runOnUiThread {

                    toast?.cancel()

                    if (ssid.startsWith("switch")) {
                        imageAnalysis.clearAnalyzer()

                        val action =
                            ScannerFragmentDirections.actionScannerFragmentToConnectingFragment(
                                ConnectionType.SCANNER,
                                ssid,
                                password,
                                type
                            )
                        findNavController().navigate(action)
                    } else {
                        toast = Toast.makeText(requireContext(), getString(R.string.invalid_qr_scan), Toast.LENGTH_LONG)
                        toast?.show()
                    }

                }
            }
        }

        val analyzer: ImageAnalysis.Analyzer = BarcodeAnalyzer(ScanningListener())
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }
}