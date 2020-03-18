package cz.covid19cz.app.ui.login

import android.os.Build
import java.util.*

data class UserModel(
    val buid: String,
    val phoneNumber: String,
    val platform: String = "android",
    val platformVersion: String = Build.VERSION.RELEASE,
    val manufacturer: String = Build.MANUFACTURER,
    val model: String = Build.MODEL,
    val locale: String = Locale.getDefault().toString(),
    val infected: Boolean = false
)