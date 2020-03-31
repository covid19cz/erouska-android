package cz.covid19cz.erouska.service

import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cz.covid19cz.erouska.AppConfig.FIREBASE_REGION
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.utils.Auth
import cz.covid19cz.erouska.utils.L
import org.koin.android.ext.android.inject

class PushService : FirebaseMessagingService() {

    val prefs: SharedPrefsRepository by inject()

    override fun onMessageReceived(message: RemoteMessage) {
        L.d("Push received: ${message.data}")
    }

    override fun onNewToken(token: String) {
        L.d("New push token: $token")
        if (Auth.isSignedIn()) {
            val data = hashMapOf(
                "buid" to prefs.getDeviceBuid(),
                "pushRegistrationToken" to token
            )
            Firebase.functions(FIREBASE_REGION).getHttpsCallable("changePushToken").call(data)
                .addOnSuccessListener {
                    L.d("Push token updated")
                }.addOnFailureListener {
                L.e(it)
            }
        }
    }
}