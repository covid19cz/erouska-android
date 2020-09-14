package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import android.os.Bundle
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.utils.L
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
    private val analytics = FirebaseAnalytics.getInstance(context)

    override suspend fun doWork(): Result {
        analytics.logEvent("key_export_download_started", Bundle())
        L.i("Starting periodical key download")
        val files = serverRepository.downloadKeyExport()
        L.i("Extracted ${files.size} files")
        exposureNotificationsRepository.provideDiagnosisKeys(files)
        analytics.logEvent("key_export_download_finished", Bundle())
        return Result.success()
    }
}