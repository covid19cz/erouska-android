package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

class DownloadKeysWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams), KoinComponent {

    companion object {
        const val TAG = "DOWNLOAD_KEYS"
        const val PERIOD = 6L
    }

    private val exposureNotificationsRepository: ExposureNotificationsRepository by inject()
    private val serverRepository: ExposureServerRepository by inject()

    override fun doWork(): Result = runBlocking {
        L.i("Starting periodical key download")
        val files = serverRepository.downloadKeyExport()
        L.i("Extracted ${files.size} files")
        exposureNotificationsRepository.provideDiagnosisKeys(files)
        Result.success()
    }

}