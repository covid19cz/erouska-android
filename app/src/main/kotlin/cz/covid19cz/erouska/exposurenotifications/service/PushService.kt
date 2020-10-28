package cz.covid19cz.erouska.exposurenotifications.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PushService : FirebaseMessagingService() {

    @Inject
    lateinit var firebaseFunctionsRepository: FirebaseFunctionsRepository

    @Inject
    lateinit var exposureNotificationsServerRepository: ExposureServerRepository

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        L.d("Push message received: ${message.data}")
        if (message.data.containsKey("downloadKeyExport")) {
            exposureNotificationsServerRepository.scheduleKeyDownload()
        }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        L.d("New push token: $newToken")
        if (FirebaseAuth.getInstance().currentUser != null) {
            GlobalScope.launch {
                try {
                    firebaseFunctionsRepository.changePushToken(newToken)
                } catch (e: Throwable) {
                    L.e(e)
                }
            }
        }
    }
}