package cz.covid19cz.erouska.exposurenotifications

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.utils.L

object InAppUpdateHelper {

    const val APP_FORCE_UPDATE_REQUEST_CODE = 1777

    /**
     * Check for update and once it's available show force update dialog.
     * Check for soft (flexible) update by default.
     */
    fun checkForAppUpdateAndUpdate(activity: Activity) {
        if (isObsolete()) {
            val appUpdateManager = AppUpdateManagerFactory.create(activity)

            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            activity,
                            APP_FORCE_UPDATE_REQUEST_CODE
                        )
                    } catch (e: Throwable) {
                        L.e(e)
                    }
                }
            }
        }
    }

    /**
     * Check for update and once it's available show local notification.
     */
    fun checkForAppUpdateAndNotify(context: Context, onSuccess: () -> Unit) {
        if (isObsolete()) {
            val appUpdateManager = AppUpdateManagerFactory.create(context)

            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    try {
                        LocalNotificationsHelper.showAppUpdateNotification(context)
                        onSuccess()
                    } catch (e: IntentSender.SendIntentException) {
                        L.e(e)
                    }
                }
            }
        }
    }

    private fun isObsolete(): Boolean {
        return BuildConfig.VERSION_CODE < AppConfig.minSupportedVersionCodeAndroid
    }
}