package ca.codefusion.switchtransfertool

import android.app.Application
import ca.codefusion.switchtransfertool.utils.CrashlyticsTree
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber


class SwitchTransferToolApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Print to logcat if in debug mode
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (hasGooglePlayServices()) {
            Timber.plant(CrashlyticsTree())
        }

        setupKoin()
    }

    private fun setupKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SwitchTransferToolApplication)
            modules(
                listOf(
                    KoinModules.appModule,
                    KoinModules.interactorModule,
                    KoinModules.viewModelModule
                )
            )
            koin.createRootScope()
        }
    }

    private fun hasGooglePlayServices(): Boolean {
        val avail = GoogleApiAvailability.getInstance()
        val resultCode = avail.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            return false
        }
        return true
    }
}
