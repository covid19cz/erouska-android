package cz.covid19cz.erouska.exposurenotifications

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.DeviceInfo
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton


/**
 * This class is heavily inspired by Swiss COVID app:
 * https://github.com/DP-3T/dp3t-app-android-ch/blob/1cc2f7cef39206a09ad74ddbdcce69dd7af7d03b/app/src/main/java/ch/admin/bag/dp3t/util/ENExceptionHelper.java
 */
@Singleton
class ExposureNotificationsErrorHandling @Inject constructor(
    private val deviceInfo: DeviceInfo,
    private val supportEmailGenerator: SupportEmailGenerator
) {

    companion object {
        const val REQUEST_GMS_ERROR_RESOLUTION = 42
        private const val ERROR_CODE_UNKNOWN = -2
    }

    private val CONNECTION_RESULT_PATTERN: Pattern =
        Pattern.compile("ConnectionResult\\{[^}]*statusCode=[a-zA-Z0-9_]+\\((\\d+)\\)")

    fun handle(gmsApiErrorEvent: GmsApiErrorEvent, fragment: Fragment, screen: String) {
        if (gmsApiErrorEvent.throwable is ApiException) {
            try {
                fragment.startIntentSenderForResult(
                    gmsApiErrorEvent.throwable.status.resolution?.intentSender,
                    REQUEST_GMS_ERROR_RESOLUTION,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (t: Throwable) {
                showErrorDialog(fragment, gmsApiErrorEvent.throwable, screen)
            }
        } else {
            showErrorDialog(fragment, gmsApiErrorEvent.throwable, screen)
        }
    }

    private fun showErrorDialog(fragment: Fragment, throwable: Throwable, screen: String) {
        val errorMessage = getErrorMessage(throwable, fragment.requireContext())
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(fragment.getString(R.string.activation_error))
            .setMessage(
                fragment.getString(
                    R.string.activation_error_reason,
                    errorMessage
                )
            )
            .setPositiveButton(R.string.support_request_button) { _, _ ->
                supportEmailGenerator.sendSupportEmail(
                    fragment.requireActivity(),
                    fragment.lifecycleScope,
                    errorCode = errorMessage,
                    isError = true,
                    screenOrigin = screen
                )
            }.setNegativeButton(R.string.send_data_close) { _, _ -> }.show()
    }

    private fun getErrorMessage(exception: Throwable, context: Context): String {
        var errorDetailMessage: String? = null
        var attachExceptionMessage = true
        if (exception is ApiException) {
            val status = exception.status
            if (status.statusCode == CommonStatusCodes.API_NOT_CONNECTED && status.statusMessage != null) {
                when (val connectionStatusCode: Int = getConnectionStatusCode(status)) {
                    ExposureNotificationStatusCodes.FAILED_NOT_SUPPORTED -> if (!deviceInfo.supportsBLE()) {
                        errorDetailMessage =
                            context.getString(R.string.activation_error_reason_bluetooth_le)
                        attachExceptionMessage = false
                    } else if (!deviceInfo.isUserDeviceOwner()) {
                        errorDetailMessage =
                            context.getString(R.string.activation_error_reason_admin)
                        attachExceptionMessage = false
                    } else if (!deviceInfo.supportsMultiAds()) {
                        errorDetailMessage =
                            context.getString(R.string.activation_error_reason_bluetooth_ad)
                    } else {
                        errorDetailMessage =
                            context.getString(R.string.activation_error_reason_en_api)
                    }
                    ExposureNotificationStatusCodes.FAILED_UNAUTHORIZED -> {
                        errorDetailMessage =
                            context.getString(R.string.activation_error_reason_unauthorized)
                    }
                    else -> errorDetailMessage =
                        ExposureNotificationStatusCodes.getStatusCodeString(connectionStatusCode)
                }
            }
        }
        return if (errorDetailMessage != null) {
            if (attachExceptionMessage) {
                "$errorDetailMessage\n${exception.message}"
            } else {
                errorDetailMessage
            }
        } else {
            exception.message ?: ""
        }
    }

    private fun getConnectionStatusCode(status: Status): Int {
        val statusMessage = status.statusMessage
        if (statusMessage != null) {
            val matcher: Matcher = CONNECTION_RESULT_PATTERN.matcher(statusMessage)
            if (matcher.find()) {
                val connectionStatusCode: String? = matcher.group(1)
                return connectionStatusCode?.toInt() ?: ERROR_CODE_UNKNOWN
            }
        }
        return ERROR_CODE_UNKNOWN
    }
}