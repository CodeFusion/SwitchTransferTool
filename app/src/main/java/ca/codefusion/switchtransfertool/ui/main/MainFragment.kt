package ca.codefusion.switchtransfertool.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.codefusion.switchtransfertool.R
import ca.codefusion.switchtransfertool.SwitchTransferToolActivity
import ca.codefusion.switchtransfertool.data.localstorage.LocalRepository
import ca.codefusion.switchtransfertool.data.models.MediaFile
import ca.codefusion.switchtransfertool.databinding.MainFragmentBinding
import ca.codefusion.switchtransfertool.helpers.safeLet
import ca.codefusion.switchtransfertool.ui.core.BaseFragment
import ca.codefusion.switchtransfertool.ui.gallery.GalleryAdapter
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject
import java.util.*


class MainFragment : BaseFragment<MainViewModel, MainFragmentBinding>() {

    override val viewModelClass = MainViewModel::class
    override val layoutResId = R.layout.main_fragment

    private val localRepository: LocalRepository by inject()

    private var mediaList: List<MediaFile> = listOf()
    private var galleryAdapter: GalleryAdapter? = GalleryAdapter(mediaList)
    private lateinit var videoPlayer: SimpleExoPlayer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.galleryRecyclerview.adapter = galleryAdapter
        binding.galleryRecyclerview.layoutManager = GridLayoutManager(requireContext(), 3)

        videoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        binding.fullVideo.player = videoPlayer
        binding.fullVideo.controllerShowTimeoutMs = 1000

        val videoView = binding.fullVideo

        checkStoragePermissions()

        videoView.context.apply {
            videoView.findViewById<ImageButton>(R.id.btn_open_external).setOnClickListener {
                viewModel.previewItem.value?.let { mediaFile ->
                    openChooser(mediaFile)
                }
            }
            videoView.findViewById<ImageButton>(R.id.btn_share).setOnClickListener {
                viewModel.previewItem.value?.let { mediaFile ->
                    shareChooser(mediaFile)
                }
            }
            videoView.findViewById<ImageButton>(R.id.btn_trash).setOnClickListener {
                viewModel.previewItem.value?.let { mediaFile ->
                    deleteMedia()
                }
            }
        }

        viewModel.generateGallery(requireContext())

        viewModel.transferButtonEvent.observe(viewLifecycleOwner) {
            viewModel.navigateToScanner(this@MainFragment)
        }

        viewModel.galleryItems.observe(viewLifecycleOwner) { galleryItems ->
            mediaList = galleryItems
            galleryAdapter = GalleryAdapter(mediaList)
            binding.galleryRecyclerview.adapter = galleryAdapter
            galleryAdapter?.setItemClickListener { selectedIndex ->
                val selectedItem = mediaList[selectedIndex]
                viewModel.showPreview(selectedItem)
            }
            if (galleryItems.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.emptyState.visibility = View.GONE
            }
        }

        // ViewModel Observers
        viewModel.openExternalClicked.observe(viewLifecycleOwner) {
            openChooser(it.peekContent())
        }
        viewModel.shareClicked.observe(viewLifecycleOwner) {
            shareChooser(it.peekContent())
        }

        viewModel.deleteClicked.observe(viewLifecycleOwner) {
            deleteMedia()
        }

        viewModel.settingsClicked.observe(viewLifecycleOwner) {
            viewModel.showSettings(requireContext())
        }

        viewModel.showPreview.observe(viewLifecycleOwner) {
            safeLet(it, viewModel.previewItem.value) { show, previewItem ->
                if (show && previewItem.type == MediaFile.MediaType.VIDEO) {
                    videoPlayer.setMediaItem(MediaItem.fromUri(previewItem.contentUri))
                    videoPlayer.prepare()
                }
                if (!show) {
                    videoPlayer.stop()
                }
            }
        }

        // Back Listener
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (viewModel.showPreview.value!!) {
                    // close overlay if visible
                    viewModel.backPressed()
                } else if (requireActivity().supportFragmentManager.backStackEntryCount == 0) {
                    // close app if no back stack
                    requireActivity().finish()
                }
            }
        })

        if (localRepository.isFirstRun()) {
            firstRun()
        } else {
            changelog()
        }

        // Shrink/expand FAB based on recyclerview scroll offset
        binding.galleryRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val vOffset = recyclerView.computeVerticalScrollOffset()
                if (vOffset == 0 && !binding.transferButton.isExtended) {
                    binding.transferButton.extend()
                } else if (vOffset != 0 && binding.transferButton.isExtended) {
                    binding.transferButton.shrink()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    // Chooser for opening externally
    fun openChooser(file: MediaFile) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(file.contentUri, when (file.type) {
                MediaFile.MediaType.IMAGE -> "image/jpeg"
                MediaFile.MediaType.VIDEO -> "video/mp4"
            })
        }
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.open_external)))
    }

    private fun deleteMedia() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirm_body)
            .setNegativeButton(R.string.nope) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setPositiveButton(R.string.yes_sure) { dialogInterface, _ ->
                viewModel.deletePreviewFile(requireContext(), requireContext() as SwitchTransferToolActivity)
            }
            .show()
    }

    // Chooser for sharing
    fun shareChooser(file: MediaFile) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, file.contentUri)
            type = when (file.type) {
                MediaFile.MediaType.IMAGE -> "image/jpeg"
                MediaFile.MediaType.VIDEO -> "video/mp4"
            }
        }
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.share)))
    }

    // Actions to take on first run
    private fun firstRun() {
        localRepository.setIsFirstRun(false)
        val pInfo = if (Build.VERSION.SDK_INT >= 33) {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        }
        val version = pInfo.versionCode
        localRepository.setLastViewedChangelogVersion(version)
    }

    // Display changelog
    private fun changelog() {
        val lastViewed = localRepository.getLastViewedChangelogVersion()
        val pInfo = if (Build.VERSION.SDK_INT >= 33) {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        }
        val version = pInfo.versionCode
        if (lastViewed < version) {
            val alert = MaterialAlertDialogBuilder(requireContext())
            alert.setTitle(R.string.whats_new_title)

            val wv = WebView(requireContext())
            wv.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return if (request != null && (request.url.toString().startsWith("http://") || request.url.toString().startsWith("https://"))) {
                        requireContext().startActivity(
                            Intent(Intent.ACTION_VIEW, request.url)
                        )
                        true
                    } else {
                        false
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    alert.show()
                }
            }
            wv.setBackgroundColor(Color.TRANSPARENT)
            alert.setView(wv)
            alert.setPositiveButton(getString(R.string.okay)) { dialog, _ ->
                dialog.dismiss()
            }
            alert.setCancelable(false)
            wv.loadUrl("file:///android_asset/changelog.html")

            localRepository.setLastViewedChangelogVersion(version)
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                viewModel.generateGallery(requireContext())
            }
        }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { _ ->
            viewModel.generateGallery(requireContext())
        }

    private fun checkStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return checkStoragePermissionsExternalStorage()
        }
        return checkStoragePermissionsMedia()
    }

    // For pre-tiramisu
    private fun checkStoragePermissionsExternalStorage() {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) ==
                    PackageManager.PERMISSION_GRANTED -> return
            shouldShowRequestPermissionRationale(permission) -> {
                return
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    // Tiramisu+
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkStoragePermissionsMedia() {
        val permissions = arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)

        val imagesPerm = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
        val videoPerm = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO)

        if (imagesPerm != PackageManager.PERMISSION_GRANTED || videoPerm != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO)) {
                return;
            } else {
                requestMultiplePermissionsLauncher.launch(permissions)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.generateGallery(requireContext())
    }
}