package ca.codefusion.switchtransfertool

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import ca.codefusion.switchtransfertool.databinding.SwitchTransferToolActivityBinding
import ca.codefusion.switchtransfertool.helpers.PrefsNames
import ca.codefusion.switchtransfertool.ui.core.BaseActivity
import com.chillibits.simplesettings.tool.getPrefStringValue
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class SwitchTransferToolActivity : BaseActivity<SwitchTransferToolViewModel, SwitchTransferToolActivityBinding>() {

    override val viewModelClass = SwitchTransferToolViewModel::class
    override val layoutResId = R.layout.switch_transfer_tool_activity

    override fun onStart() {
        super.onStart()

        val themeMode = when (this.getPrefStringValue(PrefsNames.THEME, "0")) {
            "1" -> AppCompatDelegate.MODE_NIGHT_NO
            "2" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(themeMode)

        // On every navigation action, write an analytics entry
        findNavController(binding.navHostFragmentview.id).addOnDestinationChangedListener { _, destination, args ->
            val params = Bundle()
            params.putString("fragment_name", destination.navigatorName)
            if (args?.isEmpty == false) {
                params.putString("arguments", args.toString())
            }
            FirebaseAnalytics.getInstance(this).logEvent("screen_view", params)
        }
    }
}