package cz.covid19cz.erouska

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import arch.BaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class App : BaseApp(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        AppConfig.fetchRemoteConfig()
        AndroidThreeTen.init(this)
        LocalNotificationsHelper.createNotificationChannels(this)
        removeObsoleteData()

        // Init WorkManager with app context, battery saver prevention
        WorkManager.getInstance(this)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun removeObsoleteData() {
        val obsoleteDb = File(filesDir.parent + "/databases/android-devices.db")
        if (obsoleteDb.exists()) {
            obsoleteDb.delete()
        }
    }
}
