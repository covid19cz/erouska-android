package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.utils.Analytics
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

    private val exposureNotificationsRepository: ExposureNotificationsRepository by inject()
    private val serverRepository: ExposureServerRepository by inject()

    override suspend fun doWork(): Result {
        Analytics.logEvent(context, Analytics.KEY_EXPORT_DOWNLOAD_STARTED)
        L.i("Starting periodical key download")
        val files = serverRepository.downloadKeyExport()
        L.i("Extracted ${files.size} files")
        exposureNotificationsRepository.provideDiagnosisKeys(files)
        Analytics.logEvent(context, Analytics.KEY_EXPORT_DOWNLOAD_FINISHED)
        exposureNotificationsRepository.checkExposure(context)
        return Result.success()
    }
}