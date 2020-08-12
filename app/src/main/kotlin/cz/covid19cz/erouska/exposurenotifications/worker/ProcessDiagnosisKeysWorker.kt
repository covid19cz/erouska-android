package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import androidx.work.*
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Worker that downloads and processes diagnosis keys.
 */
class ProcessDiagnosisKeysWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    companion object {
        private const val TAG = "ProcessDiagnosisKeysWorker"

        private const val ARGUMENT_TOKEN = "token"

        /**
         * Enqueue download and processing diagnosis keys.
         */
        fun enqueueProcessingOfDiagnosisKeys(workManager: WorkManager, token: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // internet access
                .build()

            val request = OneTimeWorkRequestBuilder<ProcessDiagnosisKeysWorker>()
                .setConstraints(constraints)
                .setInputData(
                    Data.Builder()
                        .putString(ARGUMENT_TOKEN, token)
                        .build()
                )
                .addTag(TAG)
                .build()

            workManager.enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    private val exposureNotificationsRepository: ExposureNotificationsRepository by inject()

    override suspend fun doWork(): Result {
        val token = inputData.getString(ARGUMENT_TOKEN)
        try {
            token?.let {
                //exposureNotificationsRepository.provideDiagnosisKeys(token)
                return@doWork Result.success()
            }
        } catch (ex: Exception){

        }
        return Result.failure()
    }
}