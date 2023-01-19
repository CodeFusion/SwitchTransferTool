package ca.codefusion.switchtransfertool.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree() : Timber.Tree() {

    private val KEY_PRIORITY = "priority"
    private val KEY_TAG = "tag"
    private val KEY_MESSAGE = "message"

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.VERBOSE, Log.DEBUG, Log.INFO -> return
            else -> {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.setCustomKey(KEY_PRIORITY, priority)
                if (tag != null) crashlytics.setCustomKey(KEY_TAG, tag)

                if (t !== null) {
                    crashlytics.setCustomKey(KEY_MESSAGE, message)
                    FirebaseCrashlytics.getInstance().recordException(t)
                } else {
                    FirebaseCrashlytics.getInstance().log(message)
                }
            }
        }
    }
}