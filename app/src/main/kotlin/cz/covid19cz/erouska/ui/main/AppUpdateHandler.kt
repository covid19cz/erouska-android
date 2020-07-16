package cz.covid19cz.erouska.ui.main

import android.app.Activity
import android.content.IntentSender
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import cz.covid19cz.erouska.utils.L

class AppUpdateHandler {
    private lateinit var appUpdateManager: AppUpdateManager

    fun checkForAppUpdate(activity: Activity) {
        appUpdateManager = AppUpdateManagerFactory.create(activity)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        APP_UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    L.e(e)
                }

            }
        }
    }

    companion object {
        const val APP_UPDATE_REQUEST_CODE = 1777
    }
}