package cz.covid19cz.erouska.exposurenotifications

import android.bluetooth.BluetoothAdapter
import com.google.android.gms.nearby.exposurenotification.*
import com.google.android.gms.tasks.Task
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.dashboard.event.BluetoothDisabledEvent
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ExposureNotificationsRepository(
    private val exposureNotificationClient: ExposureNotificationClient,
    private val btAdapter: BluetoothAdapter,
    private val prefs: SharedPrefsRepository
) {

    fun isBluetoothEnabled() : Boolean{
        return btAdapter.isEnabled
    }

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

    suspend fun provideDiagnosisKeys(
        files: List<File>,
        config: ExposureConfiguration,
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

    @Deprecated("v1 mode")
    suspend fun getExposureSummary(token: String): ExposureSummary =
        suspendCoroutine { cont ->
            exposureNotificationClient.getExposureSummary(token)
                .addOnSuccessListener {
                    cont.resume(it)
                }.addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

    suspend fun getTemporaryExposureKeyHistory() : List<TemporaryExposureKey> = suspendCoroutine { cont ->
        exposureNotificationClient.temporaryExposureKeyHistory.addOnSuccessListener {
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