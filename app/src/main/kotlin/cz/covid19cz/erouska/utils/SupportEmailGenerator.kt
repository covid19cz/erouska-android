package cz.covid19cz.erouska.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.DailySummariesDb
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportEmailGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceInfo: DeviceInfo,
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val prefs: SharedPrefsRepository,
    private val db: DailySummariesDb
) {
    fun sendSupportEmail(
        activity: Activity,
        scope: CoroutineScope,
        recipient: String = AppConfig.supportEmail,
        errorCode: String? = null,
        isError: Boolean,
        screenOrigin: String
    ) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.support_request)
            .setPositiveButton(R.string.support_request_allowed) { _, _ ->
                scope.launch {
                    activity.sendEmail(
                        recipient,
                        R.string.support_email_subject,
                        getDiagnosticFile(errorCode, screenOrigin),
                        getEmailBodyText(isError)
                    )
                }
            }
            .setNegativeButton(R.string.support_request_denied) { _, _ ->
                activity.sendEmail(
                    recipient,
                    R.string.support_email_subject,
                    emailBody = getEmailBodyText(isError)
                )
            }.show()
    }

    private suspend fun getDiagnosticFile(errorCode: String?, screenOrigin: String): Uri {
        return withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, context.getString(R.string.support_file_name))
            file.writeText(generateDiagnosticText(errorCode, screenOrigin))
            FileProvider.getUriForFile(
                context,
                context.getString(R.string.fileprovider_authorities),
                file
            )
        }
    }

    private suspend fun generateDiagnosticText(errorCode: String?, screenOrigin: String): String {
        return withContext(Dispatchers.Default) {
            val lastExposureDaysSinceEpoch = db.dao().getLatest().firstOrNull()?.daysSinceEpoch
            val lastNotifiedExposureImportTimestamp =
                db.dao().getLastNotified().firstOrNull()?.importTimestamp
            var text =
                if (errorCode != null) formatLine(R.string.support_error_code, errorCode) else ""
            text += formatLine(R.string.support_app_version, BuildConfig.VERSION_NAME)
            text += formatLine(
                R.string.support_system_version,
                "Android ${deviceInfo.getAndroidVersion()}"
            )
            text += formatLine(
                R.string.support_device,
                "${deviceInfo.getManufacturer()} ${deviceInfo.getDeviceName()}"
            )
            text += formatLine(R.string.support_localization, LocaleUtils.getLocale())
            text += formatLine(
                R.string.support_bluetooth,
                "${
                deviceInfo.isBtEnabled().toOnOff()
                } (${
                deviceInfo.supportsBLE().toSupports("BLE")
                }, ${deviceInfo.supportsMultiAds().toSupports("MultiAds")})"
            )
            text += formatLine(
                R.string.support_location_services,
                deviceInfo.isLocationEnabled().toOnOff()
            )
            text += formatLine(
                "Exposure API",
                exposureNotificationsRepository.getStatus().joinToString { it.name }
            )
            text += formatLine(
                R.string.support_primary_account,
                deviceInfo.isUserDeviceOwner().toOnOff()
            )
            text += formatLine("Internet", context.isNetworkAvailable().toOnOff())
            text += formatLine("Battery saver", deviceInfo.isBatterySaverOn().toOnOff())
            text += formatLine(
                R.string.support_battery_optimization_exception,
                deviceInfo.isIgnoringBatteryOptimizations().toOnOff()
            )
            text += formatLine(R.string.support_installation, context.getInstallDay())
            text += formatLine(
                R.string.support_last_key_download,
                prefs.getLastKeyImport().timestampToDateTime()
            )
            text += formatLine(
                R.string.support_last_notified_risky_encounter,
                lastNotifiedExposureImportTimestamp?.timestampToDateTime() ?: "N/A"
            )
            text += formatLine(
                R.string.support_last_risky_encounter_from,
                lastExposureDaysSinceEpoch?.daysSinceEpochToDateString() ?: "N/A"
            )
            text += formatLine(
                R.string.support_screen_origin,
                screenOrigin
            )
            text
        }
    }

    @StringRes
    private fun getEmailBodyText(isError: Boolean): Int {
        return if (isError) {
            R.string.support_email_body_error
        } else {
            R.string.support_email_body_contact
        }
    }

    private fun formatLine(label: Int, value: String): String {
        return "${context.getString(label)}: $value\n"
    }

    private fun formatLine(label: String, value: String): String {
        return "$label: $value\n"
    }

    private fun Boolean.toOnOff(): String {
        return if (this) "ON" else "OFF"
    }

    private fun Boolean.toSupports(subject: String): String {
        return if (this) "${context.getString(R.string.support_bluetooth_supports)} $subject" else "${
        context.getString(
            R.string.support_bluetooth_doesnt_support
        )
        } $subject"
    }
}




