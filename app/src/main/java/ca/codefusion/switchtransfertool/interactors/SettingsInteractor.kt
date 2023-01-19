package ca.codefusion.switchtransfertool.interactors

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.preference.Preference
import ca.codefusion.switchtransfertool.BuildConfig
import ca.codefusion.switchtransfertool.R
import ca.codefusion.switchtransfertool.helpers.PrefsNames
import com.chillibits.simplesettings.clicklistener.DialogClickListener
import com.chillibits.simplesettings.core.SimpleSettings
import com.mikepenz.aboutlibraries.LibsBuilder

class SettingsInteractor {
    fun showSettings(context: Context) {
        val sortOrderArray = context.resources.getStringArray(R.array.sortorder_array) as Array<String>
        val themeArray = context.resources.getStringArray(R.array.theme_array) as Array<String>

        SimpleSettings(context).show {
            Section {
                ListPref {
                    title = context.getString(R.string.theme_title)
                    simpleSummaryProvider = true
                    entries = themeArray.toList()
                    defaultIndex = 0
                    key = PrefsNames.THEME
                }
                ListPref {
                    title = context.getString(R.string.sortorder_title)
                    simpleSummaryProvider = true
                    entries = sortOrderArray.toList()
                    defaultIndex = 0
                    key = PrefsNames.GALLERY_SORT_ORDER
                }
                SwitchPref {
                    title = context.getString(R.string.capture_date_title)
                    key = PrefsNames.USE_CAPTURE_DATE
                    defaultValue = false
                    summaryOff = context.getString(R.string.capture_date_summary_off)
                    summaryOn = context.getString(R.string.capture_date_summary_on)
                }
                SwitchPref {
                    title = context.getString(R.string.default_manual_mode)
                    key = PrefsNames.MANUAL_MODE
                    defaultValue = false
                    summaryOff = context.getString(R.string.default_manual_mode_summary_off)
                    summaryOn = context.getString(R.string.default_manual_mode_summary_on)
                }
                SwitchPref {
                    title = context.getString(R.string.show_qr_tutorial)
                    key = PrefsNames.SHOW_QR_TUTORIAL
                    defaultValue = true
                    summaryOff = context.getString(R.string.show_tutorial_summary_off)
                    summaryOn = context.getString(R.string.show_tutorial_summary_on)
                }
                SwitchPref {
                    title = context.getString(R.string.show_manual_tutorial)
                    key = PrefsNames.SHOW_MANUAL_TUTORIAL
                    defaultValue = true
                    summaryOff = context.getString(R.string.show_tutorial_summary_off)
                    summaryOn = context.getString(R.string.show_tutorial_summary_on)
                }
            }
            Section {
                title = context.getString(R.string.about_prefs_section)
                TextPref {
                    title = context.getString(R.string.app_version_title)
                    summary = BuildConfig.VERSION_NAME
                }
                TextPref {
                    title = context.getString(R.string.source_title)
                    summary = context.getString(R.string.source_summary)
                    onClick = Preference.OnPreferenceClickListener {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/CodeFusion/SwitchTransferTool")
                            )
                        )
                        true
                    }
                }
                TextPref {
                    title = context.getString(R.string.icons_credit)
                    onClick = Preference.OnPreferenceClickListener {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://jesseramey.ca")
                            )
                        )
                        true
                    }
                }
                TextPref {
                    title = context.getString(R.string.oss_licenses_title)
                    onClick = Preference.OnPreferenceClickListener { showOssLicenses(context) }
                }
                TextPref {
                    title = context.getString(R.string.disclaimer_title)
                    onClick = DialogClickListener() {
                        title = context.getString(R.string.disclaimer_title)
                        message = context.getString(R.string.non_affiliation_disclaimer)
                        cancelable = true
                        type = DialogClickListener.Type.OK
                    }
                }
            }
        }
    }

    private fun showOssLicenses(context: Context): Boolean {
        LibsBuilder()
            .withActivityTitle(context.getString(R.string.oss_licenses_title))
            .withAboutVersionShownName(false)
            .withAboutIconShown(false)
            .withAboutVersionShownCode(false)
            .withFields(R.string::class.java.fields)
            .withEdgeToEdge(true)
            .start(context)
        return true
    }
}