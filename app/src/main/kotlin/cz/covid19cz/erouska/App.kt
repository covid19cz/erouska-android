package cz.covid19cz.erouska

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import arch.BaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import cz.covid19cz.erouska.exposurenotifications.Notifications
import cz.covid19cz.erouska.exposurenotifications.worker.DownloadKeysWorker
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class App : BaseApp(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notifications: Notifications

    override fun onCreate() {
        super.onCreate()
        AppConfig.fetchRemoteConfig()
        AndroidThreeTen.init(this)
        notifications.init()
        removeObsoleteData()

        // Init WorkManager with app context, battery saver prevention
        WorkManager.getInstance(this)

        //TODO: Remove if eRouška gets resurrected
        unscheduleWorkers()
    }

    private fun unscheduleWorkers(){
        WorkManager.getInstance(this).cancelAllWork()
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
