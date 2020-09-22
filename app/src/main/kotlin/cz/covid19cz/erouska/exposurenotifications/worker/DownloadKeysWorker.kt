package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cz.covid19cz.erouska.allModules
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.utils.Analytics
import cz.covid19cz.erouska.utils.L
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject

class DownloadKeysWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        const val TAG = "DOWNLOAD_KEYS"
    }

    override suspend fun doWork(): Result {
        try {
            val exposureNotificationsRepository: ExposureNotificationsRepository by inject()
            val serverRepository: ExposureServerRepository by inject()
            if (exposureNotificationsRepository.isEligibleToDownloadKeys()) {
                L.i("Starting download keys worker")
                Analytics.logEvent(context, Analytics.KEY_EXPORT_DOWNLOAD_STARTED)
                val result = serverRepository.downloadKeyExport()

                exposureNotificationsRepository.provideDiagnosisKeys(result)
                Analytics.logEvent(context, Analytics.KEY_EXPORT_DOWNLOAD_FINISHED)
                exposureNotificationsRepository.checkExposure(context)
            } else {
                L.i("Skipping download keys worker")
            }
            return Result.success()
        } catch (t : Throwable){
            L.e(t)
            return Result.failure()
        }
    }
}