package ca.codefusion.switchtransfertool.data.localstorage

import android.content.Context
import androidx.preference.PreferenceManager
import ca.codefusion.switchtransfertool.helpers.PrefsNames
import com.chillibits.simplesettings.tool.getPrefBooleanValue
import com.chillibits.simplesettings.tool.getPrefIntValue

class SharedPrefsRepository(private val context: Context) : LocalRepository {

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getGallerySortDirection() =
        context.getPrefBooleanValue(PrefsNames.GALLERY_SORT_ORDER, false)

    override fun setGallerySortDirection(desc: Boolean) {
        sharedPrefs.edit().run {
            putBoolean(PrefsNames.GALLERY_SORT_ORDER, desc)
            commit()
        }
    }

    override fun getShowQRTutorial() = context.getPrefBooleanValue(PrefsNames.SHOW_QR_TUTORIAL, true)

    override fun setShowQRTutorial(shouldShow: Boolean) {
        sharedPrefs.edit().run {
            putBoolean(PrefsNames.SHOW_QR_TUTORIAL, shouldShow)
            commit()
        }
    }

    override fun getShowManualTutorial() =
        context.getPrefBooleanValue(PrefsNames.SHOW_MANUAL_TUTORIAL, true)

    override fun setShowManualTutorial(shouldShow: Boolean) {
        sharedPrefs.edit().run {
            putBoolean(PrefsNames.SHOW_MANUAL_TUTORIAL, shouldShow)
            commit()
        }
    }

    override fun getLastViewedChangelogVersion() =
        context.getPrefIntValue(PrefsNames.LAST_VIEWED_CHANGELOG, 0)

    override fun setLastViewedChangelogVersion(versionCode: Int) {
        sharedPrefs.edit().run {
            putInt(PrefsNames.LAST_VIEWED_CHANGELOG, versionCode)
            commit()
        }
    }

    override fun isFirstRun() =
        context.getPrefBooleanValue(PrefsNames.FIRST_RUN, true)

    override fun setIsFirstRun(firstRun: Boolean) {
        sharedPrefs.edit().run {
            putBoolean(PrefsNames.FIRST_RUN, firstRun)
            commit()
        }
    }

    override fun isDefaultManualMode() =
        context.getPrefBooleanValue(PrefsNames.MANUAL_MODE, false)

    override fun setDefaultManualMode(useManualMode: Boolean) {
        sharedPrefs.edit().run {
            putBoolean(PrefsNames.MANUAL_MODE, useManualMode)
            commit()
        }
    }

}