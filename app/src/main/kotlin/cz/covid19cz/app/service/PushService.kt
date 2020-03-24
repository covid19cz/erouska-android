package cz.covid19cz.app.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cz.covid19cz.app.utils.L

class PushService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        L.d("Push received: ${message.data}")
    }

    override fun onNewToken(token: String) {
        L.d("New push token: $token")
    }
}