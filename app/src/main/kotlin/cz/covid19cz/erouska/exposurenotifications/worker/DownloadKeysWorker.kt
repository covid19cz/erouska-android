package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.exposurenotifications.Notifications
import cz.covid19cz.erouska.net.ExposureServerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DownloadKeysWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val serverRepository: ExposureServerRepository,
    private val notifications: Notifications
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "DOWNLOAD_KEYS"
    }

    override suspend fun doWork(): Result {
        //TODO: Remove if eRouška gets resurrected
        /**
        try {
            setForeground(ForegroundInfo(Notifications.REQ_ID_DOWNLOADING, notifications.getDownloadingNotification(id)))
            if (exposureNotificationsRepository.isEligibleToDownloadKeys()) {
                L.i("Starting download keys worker")
                Analytics.logEvent(context, Analytics.KEY_EXPORT_DOWNLOAD_STARTED)
                val result = serverRepository.downloadKeyExport()
                exposureNotificationsRepository.provideDiagnosisKeys(result)
                exposureNotificationsRepository.checkExposure()
                Analytics.logEvent(context, Analytics.KEY_EXPORT_DOWNLOAD_FINISHED)
            } else {
                L.i("Skipping download keys worker")
            }
            return Result.success()
        } catch (t: Throwable) {
            L.e(t)
            return if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }**/
        return Result.success()
    }
}