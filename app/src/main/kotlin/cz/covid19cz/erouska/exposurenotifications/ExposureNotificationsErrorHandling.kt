package cz.covid19cz.erouska.exposurenotifications

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.UserManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * This class is heavily inspired by Swiss COVID app:
 * https://github.com/DP-3T/dp3t-app-android-ch/blob/1cc2f7cef39206a09ad74ddbdcce69dd7af7d03b/app/src/main/java/ch/admin/bag/dp3t/util/ENExceptionHelper.java
 */
object ExposureNotificationsErrorHandling {

    const val REQUEST_GMS_ERROR_RESOLUTION = 42
    private const val ERROR_CODE_UNKNOWN = -2
    private val CONNECTION_RESULT_PATTERN: Pattern =
        Pattern.compile("ConnectionResult\\{[^}]*statusCode=[a-zA-Z0-9_]+\\((\\d+)\\)")

    fun handle(gmsApiErrorEvent: GmsApiErrorEvent, fragment: Fragment) {
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
                showErrorDialog(fragment.requireContext(), gmsApiErrorEvent.throwable)
            }
        } else {
            showErrorDialog(fragment.requireContext(), gmsApiErrorEvent.throwable)
        }
    }

    private fun showErrorDialog(context: Context, throwable: Throwable) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.activation_error))
            .setMessage(context.getString(R.string.activation_error_reason, getErrorMessage(throwable, context)))
            .setPositiveButton(R.string.ok) { _, _ ->
            }.show()
    }

    private fun getErrorMessage(exception: Throwable, context: Context): String {
        var errorDetailMessage: String? = null
        var attachExceptionMessage = true
        if (exception is ApiException) {
            val status = exception.status
            if (status.statusCode == CommonStatusCodes.API_NOT_CONNECTED && status.statusMessage != null) {
                when (val connectionStatusCode: Int = getConnectionStatusCode(status)) {
                    ExposureNotificationStatusCodes.FAILED_NOT_SUPPORTED -> if (!supportsBLE(context)) {
                        errorDetailMessage = context.getString(R.string.activation_error_reason_bluetooth_le)
                        attachExceptionMessage = false
                    } else if (!isUserDeviceOwner(context)) {
                        errorDetailMessage = context.getString(R.string.activation_error_reason_admin)
                        attachExceptionMessage = false
                    } else if (!supportsMultiAds()) {
                        errorDetailMessage = context.getString(R.string.activation_error_reason_bluetooth_ad)
                    } else {
                        errorDetailMessage = context.getString(R.string.activation_error_reason_en_api)
                    }
                    ExposureNotificationStatusCodes.FAILED_UNAUTHORIZED -> {
                        errorDetailMessage = context.getString(R.string.activation_error_reason_unauthorized)
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

    private fun supportsBLE(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    private fun isUserDeviceOwner(context: Context): Boolean {
        val um = context.getSystemService(Context.USER_SERVICE) as UserManager
        return um.isSystemUser
    }

    private fun supportsMultiAds(): Boolean {
        return BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported
    }
}