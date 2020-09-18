package cz.covid19cz.erouska.exposurenotifications.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.exposurenotifications.InAppUpdateHelper
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit

class SelfCheckerWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        const val TAG = "SELF_CHECKER"
    }

    private val prefs: SharedPrefsRepository by inject()
    private val exposureNotificationsRepository: ExposureNotificationsRepository by inject()

    override suspend fun doWork(): Result {
        val hour = Calendar.getInstance(Locale.getDefault()).get(Calendar.HOUR_OF_DAY)
        if (hour in 9..19) {
            if (!exposureNotificationsRepository.isEnabled()) {
                LocalNotificationsHelper.showErouskaPausedNotification(context)
            }
            if (prefs.hasOutdatedKeyData()) {
                LocalNotificationsHelper.showOutdatedDataNotification(context)
            }

            if (isEligibleToShowAppUpdateNotification()) {
                InAppUpdateHelper.checkForAppUpdateAndNotify(context) {
                    prefs.setLastTimeAppUpdateNotificationShown(System.currentTimeMillis())
                    prefs.setLastVersionAppUpdateNotificationShown(BuildConfig.VERSION_CODE)
                }
            }
        }
        return Result.success()
    }

    /**
     * App is eligible to show local notification about app update.
     * Returns true if the version is new or if the notification was shown last time before at least x-days (configurable via RC).
     */
    private fun isEligibleToShowAppUpdateNotification(): Boolean {
        return prefs.getLastVersionAppUpdateNotificationShown() != BuildConfig.VERSION_CODE ||
                System.currentTimeMillis() - prefs.getLastTimeAppUpdateNotificationShown() >= TimeUnit.DAYS.toMillis(
            AppConfig.forceAppUpdatePeriodDays
        )
    }

}