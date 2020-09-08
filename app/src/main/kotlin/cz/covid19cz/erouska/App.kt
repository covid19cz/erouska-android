package cz.covid19cz.erouska

import androidx.work.WorkManager
import arch.BaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import java.io.File

class App : BaseApp(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        setupKoin()
        AppConfig.fetchRemoteConfig()
        AndroidThreeTen.init(this)
        LocalNotificationsHelper.createNotificationChannels(this)
        removeObsoleteData()

        // Init WorkManager with app context, battery saver prevention
        WorkManager.getInstance(this)
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@App)
            modules(allModules)
        }
    }

    private fun removeObsoleteData() {
        val obsoleteDb = File(filesDir.parent + "/databases/android-devices.db")
        if (obsoleteDb.exists()) {
            obsoleteDb.delete()
        }
    }
}
