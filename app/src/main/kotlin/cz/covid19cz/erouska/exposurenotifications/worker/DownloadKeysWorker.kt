package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class DownloadKeysWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        const val TAG = "DOWNLOAD_KEYS"
    }

    private val prefs: SharedPrefsRepository by inject()
    private val exposureNotificationsRepository: ExposureNotificationsRepository by inject()
    private val serverRepository: ExposureServerRepository by inject()
    private val firebaseFunctionsRepository: FirebaseFunctionsRepository by inject()

    override suspend fun doWork(): Result {
        L.i("Starting periodical key download")
        val files = serverRepository.downloadKeyExport()
        L.i("Extracted ${files.size} files")
        exposureNotificationsRepository.provideDiagnosisKeys(files)
        checkExposure(context)
        return Result.success()
    }

    private suspend fun checkExposure(context: Context) {
        val lastExposure = exposureNotificationsRepository.getLastRiskyExposure()?.daysSinceEpoch
        val lastNotifiedExposure = prefs.getLastNotifiedExposure()
        if (lastExposure != null && lastNotifiedExposure != 0 && lastExposure != lastNotifiedExposure) {
            firebaseFunctionsRepository.registerNotification()
            LocalNotificationsHelper.showRiskyExposureNotification(context)
            prefs.setLastNotifiedExposure(lastExposure)
        }
    }
}