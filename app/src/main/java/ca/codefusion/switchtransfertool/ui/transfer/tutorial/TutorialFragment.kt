package ca.codefusion.switchtransfertool.ui.transfer.tutorial

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import ca.codefusion.switchtransfertool.R
import ca.codefusion.switchtransfertool.data.localstorage.LocalRepository
import ca.codefusion.switchtransfertool.databinding.ConnectionTutorialFragmentBinding
import ca.codefusion.switchtransfertool.ui.core.BaseFragment
import ca.codefusion.switchtransfertool.utils.ConnectionType
import org.koin.android.ext.android.inject

class TutorialFragment: BaseFragment<TutorialViewModel, ConnectionTutorialFragmentBinding>() {

    override val viewModelClass = TutorialViewModel::class
    override val layoutResId = R.layout.connection_tutorial_fragment
    private lateinit var connectionType: ConnectionType
    private lateinit var viewPager: ViewPager2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dotsIndicator = binding.dotsIndicator
        viewPager = binding.viewPager
        val adapter = TutorialViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle)
        viewPager.adapter = adapter
        dotsIndicator.setViewPager2(viewPager)

        connectionType = TutorialFragmentArgs.fromBundle(requireArguments()).connectionType

        when(connectionType) {
            ConnectionType.SCANNER -> {
                adapter.addFragment(
                    TutorialPageFragment(
                        image = ContextCompat.getDrawable(requireContext(), R.drawable.qr_tut_step1),
                        bodyText = requireContext().getString(R.string.wifi_tut_body1)
                    )
                )
                adapter.addFragment(
                    TutorialPageFragment(
                        image = ContextCompat.getDrawable(requireContext(), R.drawable.qr_tut_step2),
                        bodyText = requireContext().getString(R.string.wifi_tut_body2)
                    )
                )
                adapter.addFragment(
                    TutorialPageFragment(
                        image = ContextCompat.getDrawable(requireContext(), R.drawable.qr_tut_step3),
                        bodyText = requireContext().getString(R.string.wifi_tut_body3),
                        actionButtonText = requireContext().getString(R.string.wifi_tut_finish),
                        onActionButtonClickEvent = {
                            val action = TutorialFragmentDirections.actionTutorialFragmentToScannerFragment()
                            findNavController().navigate(action)
                        }
                    )
                )
            }
            ConnectionType.MANUAL -> {
                adapter.addFragment(
                    TutorialPageFragment(
                        image = ContextCompat.getDrawable(requireContext(), R.drawable.qr_tut_step1),
                        bodyText = requireContext().getString(R.string.manual_tut_body1)
                    )
                )
                adapter.addFragment(
                    TutorialPageFragment(
                        image = ContextCompat.getDrawable(requireContext(), R.drawable.qr_tut_step2),
                        bodyText = requireContext().getString(R.string.manual_tut_body2)
                    )
                )
                adapter.addFragment(
                    TutorialPageFragment(
                        image = ContextCompat.getDrawable(requireContext(), R.drawable.manual_tut_step1),
                        bodyText = requireContext().getString(R.string.manual_tut_body3)
                    )
                )
                adapter.addFragment(
                    TutorialPageFragment(
                        image = ContextCompat.getDrawable(requireContext(), R.drawable.manual_tut_step2),
                        bodyText = requireContext().getString(R.string.manual_tut_body4)
                    )
                )
                adapter.addFragment(
                    TutorialPageFragment(
                        image = ContextCompat.getDrawable(requireContext(), R.drawable.manual_tut_step3),
                        bodyText = requireContext().getString(R.string.manual_tut_body5),
                        actionButtonText = requireContext().getString(R.string.wifi_tut_finish),
                        onActionButtonClickEvent = {
                            onTutorialComplete(connectionType)
                        }
                    )
                )
            }
        }

        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position < adapter.itemCount - 1) {
                    viewModel.skipButtonVisible(true)
                    viewModel.nextButtonVisible(true)
                } else {
                    viewModel.skipButtonVisible(false)
                    viewModel.nextButtonVisible(false)
                }
            }
        })

        viewModel.nextButtonClick.observe(viewLifecycleOwner, {
            if (viewPager.currentItem < adapter.itemCount-1) {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        })

        viewModel.skipButtonClick.observe(viewLifecycleOwner, {
            onTutorialComplete(connectionType)
        })
    }

    private fun onTutorialComplete(connectionType: ConnectionType) {

        val action = when (connectionType) {
            ConnectionType.SCANNER -> TutorialFragmentDirections.actionTutorialFragmentToScannerFragment()
            ConnectionType.MANUAL -> TutorialFragmentDirections.actionTutorialFragmentToDownloadingFragment(
                connectionType = connectionType
            )
        }
        findNavController().navigate(action)
    }

    inner class TutorialViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle):
        FragmentStateAdapter(fragmentManager, lifecycle) {

        private val pages = ArrayList<Fragment>()

        fun addFragment(fragment: Fragment) {
            pages.add(fragment)
        }

        override fun getItemCount(): Int {
            return pages.size
        }

        override fun createFragment(position: Int): Fragment {
            return pages[position]
        }
    }
}