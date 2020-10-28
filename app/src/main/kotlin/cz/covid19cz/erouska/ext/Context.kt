package cz.covid19cz.erouska.ext

import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.L

fun Context.isBtEnabled(): Boolean {
    val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    return btManager.adapter.isEnabled
}

fun Context?.isLocationEnabled(): Boolean {
    val locationManager = this?.getSystemService(LocationManager::class.java) ?: return false
    return LocationManagerCompat.isLocationEnabled(locationManager)
}

fun Context.openPermissionsScreen() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}

@Suppress("DEPRECATION")
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    with(connectivityManager) {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            activeNetworkInfo?.isConnected
        } else {
            getNetworkCapabilities(activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
    }
}

fun Context.shareApp() {
    val text = getString(R.string.share_app_text, AppConfig.shareAppDynamicLink)
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(intent, getString(R.string.share_app_title)))
}

fun BaseFragment<*, *>.showWeb(url: String, customTabHelper: CustomTabHelper) {
    val intent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        .setCloseButtonIcon(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_action_up
            )
        )
        .build()
    if (customTabHelper.chromePackageName != null) {
        intent.launchUrl(requireContext(), Uri.parse(url))
    } else {
        // Custom Tabs not available
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        } catch (e: ActivityNotFoundException) {
            L.e(e)
        }
    }
}

fun Activity.sendEmail(recipient: String, subject: Int, file: Uri? = null) {
    val originalIntent = createEmailShareIntent(recipient, subject, file)
    val emailFilterIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
    val originalIntentResults = packageManager.queryIntentActivities(originalIntent, 0)
    val emailFilterIntentResults = packageManager.queryIntentActivities(emailFilterIntent, 0)
    val targetedIntents = originalIntentResults
        .filter { originalResult -> emailFilterIntentResults.any { originalResult.activityInfo.packageName == it.activityInfo.packageName } }
        .map {
            createEmailShareIntent(recipient, subject, file).apply { `package` = it.activityInfo.packageName }
        }
        .toMutableList()
    if (targetedIntents.size > 0) {
        val finalIntent = Intent.createChooser(targetedIntents.removeAt(0), getString(R.string.support_email_chooser))
        finalIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toTypedArray())
        startActivity(finalIntent)
    }
}

private fun Activity.createEmailShareIntent(recipient: String, subject: Int, file: Uri?): Intent {
    val builder = ShareCompat.IntentBuilder.from(this)
        .setType("message/rfc822")
        .setEmailTo(arrayOf(recipient))
        .setSubject(getString(subject))
    if (file != null) {
        builder.setStream(file)
    }
    return builder.intent
}

fun Context.getInstallDay(): String {
    val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
    val installTimestamp = packageInfo.firstInstallTime
    return if (installTimestamp > 0) {
        installTimestamp.timestampToDate()
    } else {
        "N/A"
    }
}
