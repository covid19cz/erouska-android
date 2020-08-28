package cz.covid19cz.erouska

import arch.BaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import cz.covid19cz.erouska.localnotifications.LocalNotificationsReceiver
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
        LocalNotificationsReceiver.createNotificationChannels(this)
        removeObsoleteData()
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
