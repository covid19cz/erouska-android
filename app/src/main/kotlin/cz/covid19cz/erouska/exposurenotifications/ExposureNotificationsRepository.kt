package cz.covid19cz.erouska.exposurenotifications

import com.google.android.gms.nearby.exposurenotification.*
import com.google.android.gms.tasks.Task
import cz.covid19cz.erouska.db.SharedPrefsRepository
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ExposureNotificationsRepository(
    private val exposureNotificationClient: ExposureNotificationClient,
    private val prefs: SharedPrefsRepository
) {

    val config = ExposureConfiguration.ExposureConfigurationBuilder()
        .setDurationAtAttenuationThresholds(
            prefs.getAttenuationThreshold1(50), prefs.getAttenuationThreshold2(60)
        )
        .build()

    suspend fun start() = suspendCoroutine<Void> { cont ->
        exposureNotificationClient.start()
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun stop() = suspendCoroutine<Void> { cont ->
        exposureNotificationClient.stop()
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun isEnabled(): Boolean = suspendCoroutine { cont ->
        exposureNotificationClient.isEnabled
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getTemporaryExposureKeyHistory(): List<TemporaryExposureKey> {
        return exposureNotificationClient.temporaryExposureKeyHistory.await()
    }

    suspend fun provideDiagnosisKeys(
        files: List<File>,
        token: String?
    ): Void = suspendCoroutine { cont ->
        exposureNotificationClient.provideDiagnosisKeys(
            files,
            config,
            token
        )
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getExposureSummary(token: String): ExposureSummary =
        suspendCoroutine { cont ->
            exposureNotificationClient.getExposureSummary(token)
                .addOnSuccessListener {
                    cont.resume(it)
                }.addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

    suspend fun getExposureWindows(): List<ExposureWindow> = suspendCoroutine { cont ->
        exposureNotificationClient.getExposureWindows(ExposureNotificationClient.TOKEN_A)
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }
}