package ca.codefusion.switchtransfertool.ui.transfer.downloader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import ca.codefusion.switchtransfertool.R
import ca.codefusion.switchtransfertool.databinding.DownloadingFragmentBinding
import ca.codefusion.switchtransfertool.ui.core.BaseFragment
import ca.codefusion.switchtransfertool.helpers.safeLet
import ca.codefusion.switchtransfertool.utils.ConnectionType
import timber.log.Timber

class DownloadingFragment : BaseFragment<DownloadingViewModel, DownloadingFragmentBinding>()  {

    override val viewModelClass = DownloadingViewModel::class
    override val layoutResId = R.layout.downloading_fragment

    private lateinit var connectionType: ConnectionType

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectionType = DownloadingFragmentArgs.fromBundle(requireArguments()).connectionType

        when (connectionType) {
            ConnectionType.SCANNER -> {
                safeLet(context, arguments?.getString("ssid"), arguments?.getString("password")) { safeContext, safeSsid, safePass ->
                    viewModel.connectToNetwork(safeContext, safeSsid, safePass)
                }
            }
            ConnectionType.MANUAL -> {
                viewModel.startManualConnectionCheck()
            }
        }

        val moreInfoText = SpannableString(getString(R.string.more_info))
        moreInfoText.setSpan(UnderlineSpan(), 0, moreInfoText.length, 0)
        binding.moreInfo.text = moreInfoText

        val wifiText = SpannableString(getString(R.string.open_wifi_settings))
        wifiText.setSpan(UnderlineSpan(), 0, wifiText.length, 0)
        binding.wifiSettings.text = wifiText

        viewModel.showHelp.observe(viewLifecycleOwner) {
            val action = DownloadingFragmentDirections.actionDownloadingFragmentToTutorialFragment(
                ConnectionType.MANUAL
            )
            findNavController().navigate(action)
        }

        viewModel.galleryButtonEvent.observe(viewLifecycleOwner) {
            Timber.d("Return to Gallery button pressed")
            disconnectFromNetwork()
            val action = DownloadingFragmentDirections.actionDownloadingFragmentToMainFragment()
            findNavController().navigate(action)
        }

        viewModel.moreInfoEvent.observe(viewLifecycleOwner) {
            val action = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://switchtransfertool.codefusion.ca/faq.html")
            )
            startActivity(action)
        }

        viewModel.scanButtonEvent.observe(viewLifecycleOwner) {
            val action = DownloadingFragmentDirections.actionDownloadingFragmentToScannerFragment()
            findNavController().navigate(action)
        }

        viewModel.connectionFailureEvent.observe(viewLifecycleOwner) {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.connection_failure_desc))
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }
                .setNeutralButton(R.string.more_info) { dialog, _ ->
                    val browserIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://switchtransfertool.codefusion.ca/faq.html")
                        )
                    startActivity(browserIntent)
                }
                .show()
        }

        viewModel.showWifiSettings.observe(viewLifecycleOwner) {
            showWifiPage()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                DownloadingViewModel.AWAITING_SSID -> {
                    binding.connectionLoader.visibility = View.VISIBLE
                    binding.image.visibility = View.GONE
                    binding.title.text = getString(R.string.awaiting_connection)
                    binding.desc.visibility = View.VISIBLE
                    binding.desc.text = getString(R.string.awaiting_connection_desc)
                    binding.moreInfo.visibility = View.GONE
                    binding.wifiSettings.visibility = View.VISIBLE
                    Timber.tag("DOWNLOADER_STATE").d("AWAITING_SSID")
                }
                DownloadingViewModel.CONNECTING -> {
                    binding.connectionLoader.visibility = View.VISIBLE
                    binding.image.visibility = View.GONE
                    binding.title.text = getString(R.string.connecting)
                    binding.desc.visibility = View.GONE
                    binding.moreInfo.visibility = View.GONE
                    binding.wifiSettings.visibility = View.GONE
                    Timber.tag("DOWNLOADER_STATE").d("CONNECTING")
                }
                DownloadingViewModel.DOWNLOADING -> {
                    binding.connectionLoader.visibility = View.VISIBLE
                    binding.image.visibility = View.GONE
                    binding.title.text = getString(R.string.downloading)
                    binding.desc.visibility = View.GONE
                    binding.moreInfo.visibility = View.GONE
                    binding.wifiSettings.visibility = View.GONE
                    Timber.tag("DOWNLOADER_STATE").d("DOWNLOADING")
                }
                DownloadingViewModel.SUCCESS_IMAGE -> {
                    binding.connectionLoader.visibility = View.GONE
                    binding.image.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_image,
                            requireContext().theme
                        )
                    )
                    binding.image.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.downloaded_single_image)
                    binding.desc.text = getString(R.string.download_complete_desc)
                    binding.desc.visibility = View.VISIBLE
                    binding.moreInfo.visibility = View.GONE
                    binding.wifiSettings.visibility = View.GONE

                    Timber.tag("DOWNLOADER_STATE").d("SUCCESS_IMAGE")
                    disconnectFromNetwork()
                }
                DownloadingViewModel.SUCCESS_MULTIPLE_IMAGES -> {
                    binding.connectionLoader.visibility = View.GONE
                    binding.image.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_images,
                            requireContext().theme
                        )
                    )
                    binding.image.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.downloaded_multiple_images)
                    if (viewModel.imageCount.value != null) {
                        binding.desc.text = String.format(
                            getString(R.string.download_complete_desc),
                            viewModel.imageCount.value
                        )
                    } else {
                        binding.desc.text = getString(R.string.download_complete_desc)
                    }
                    binding.desc.visibility = View.VISIBLE
                    binding.moreInfo.visibility = View.GONE
                    binding.wifiSettings.visibility = View.GONE

                    Timber.tag("DOWNLOADER_STATE").d("SUCCESS_MULTIPLE_IMAGES")
                    disconnectFromNetwork()
                }
                DownloadingViewModel.SUCCESS_VIDEO -> {
                    binding.connectionLoader.visibility = View.GONE
                    binding.image.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_video,
                            requireContext().theme
                        )
                    )
                    binding.image.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.downloaded_video)
                    binding.desc.text = getString(R.string.download_complete_desc)
                    binding.desc.visibility = View.VISIBLE
                    binding.moreInfo.visibility = View.GONE
                    binding.wifiSettings.visibility = View.GONE

                    Timber.tag("DOWNLOADER_STATE").d("SUCCESS_VIDEO")
                    disconnectFromNetwork()
                }
                DownloadingViewModel.LOST -> {
                    binding.connectionLoader.visibility = View.GONE
                    binding.image.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_phone_error,
                            requireContext().theme
                        )
                    )
                    binding.image.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.lost_connection)
                    binding.desc.text = getString(R.string.lost_connection_desc)
                    binding.desc.visibility = View.VISIBLE
                    binding.moreInfo.visibility = View.VISIBLE
                    binding.wifiSettings.visibility = View.GONE

                    Timber.tag("DOWNLOADER_STATE").d("LOST")
                    disconnectFromNetwork()
                }
                DownloadingViewModel.UNAVAILABLE -> {
                    binding.connectionLoader.visibility = View.GONE
                    binding.image.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_phone_error,
                            requireContext().theme
                        )
                    )
                    binding.image.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.connection_failure_title)
                    binding.desc.text = getString(R.string.connection_failure_desc)
                    binding.desc.visibility = View.VISIBLE
                    binding.moreInfo.visibility = View.VISIBLE
                    binding.wifiSettings.visibility = View.GONE

                    Timber.tag("DOWNLOADER_STATE").d("UNAVAILABLE")
                    disconnectFromNetwork()
                }
                DownloadingViewModel.UNKNOWN_ERROR -> {
                    binding.connectionLoader.visibility = View.GONE
                    binding.image.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_phone_error,
                            requireContext().theme
                        )
                    )
                    binding.image.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.unknown_error)
                    binding.desc.text = getString(R.string.unknown_error_desc)
                    binding.desc.visibility = View.VISIBLE
                    binding.moreInfo.visibility = View.GONE
                    binding.wifiSettings.visibility = View.GONE

                    Timber.tag("DOWNLOADER_STATE").d("UNKNOWN_ERROR")
                    disconnectFromNetwork()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.state.value == DownloadingViewModel.STATE.AWAITING_SSID) {
            viewModel.checkManualConnection()
        }
    }

    private fun disconnectFromNetwork() {
        if (connectionType == ConnectionType.SCANNER) {
            viewModel.disconnectFromNetwork(requireContext())
        }
    }

    private fun showWifiPage() {
        val panelIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivity(panelIntent)
    }
}