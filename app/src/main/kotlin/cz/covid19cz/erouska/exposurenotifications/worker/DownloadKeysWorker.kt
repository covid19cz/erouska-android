package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.utils.Analytics
import cz.covid19cz.erouska.utils.L

class DownloadKeysWorker @WorkerInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val serverRepository: ExposureServerRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "DOWNLOAD_KEYS"
    }

    override suspend fun doWork(): Result {
        try {
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