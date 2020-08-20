package cz.covid19cz.erouska.worker

import android.content.Context
import androidx.work.*
import cz.covid19cz.erouska.exposurenotifications.worker.DownloadKeysWorker
import java.util.concurrent.TimeUnit

class WorkerRepository(private val context: Context) {

    fun scheduleKeyDownload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val worker = PeriodicWorkRequestBuilder<DownloadKeysWorker>(
            20,
            TimeUnit.MINUTES
        ).setConstraints(constraints)
            .addTag(DownloadKeysWorker.TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DownloadKeysWorker.TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                worker
            )
    }

}