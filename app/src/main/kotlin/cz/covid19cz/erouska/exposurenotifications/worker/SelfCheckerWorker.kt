package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class SelfCheckerWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        const val TAG = "SELF_CHECKER"
    }

    override suspend fun doWork(): Result {
        val prefs: SharedPrefsRepository by inject()
        val exposureNotificationsRepository: ExposureNotificationsRepository by inject()
        val hour = Calendar.getInstance(Locale.getDefault()).get(Calendar.HOUR_OF_DAY)
        if (hour in 9..19) {
            if (!exposureNotificationsRepository.isEnabled()) {
                LocalNotificationsHelper.showErouskaPausedNotification(context)
            }
            if (prefs.hasOutdatedKeyData()) {
                LocalNotificationsHelper.showOutdatedDataNotification(context)
            }
        }
        return Result.success()
    }

}