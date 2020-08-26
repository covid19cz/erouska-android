package cz.covid19cz.erouska.exposurenotifications

import android.bluetooth.BluetoothAdapter
import com.google.android.gms.nearby.exposurenotification.*
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ExposureNotificationsRepository(
    private val exposureNotificationClient: ExposureNotificationClient,
    private val btAdapter: BluetoothAdapter,
    private val prefs: SharedPrefsRepository
) {

    fun isBluetoothEnabled(): Boolean {
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
        files: List<File>): Void = suspendCoroutine { cont ->
        exposureNotificationClient.provideDiagnosisKeys(
            files
        )
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun getDailySummaries(): List<DailySummary> = suspendCoroutine { cont ->

        val reportTypeWeights = prefs.getReportTypeWeights() ?: AppConfig.reportTypeWeights
        val attenuationBucketThresholdDb = prefs.getAttenuationBucketThresholdDb() ?: AppConfig.attenuationBucketThresholdDb
        val attenuationBucketWeights = prefs.getAttenuationBucketWeights() ?: AppConfig.attenuationBucketWeights
        val infectiousnessWeights = prefs.getInfectiousnessWeights() ?: AppConfig.infectiousnessWeights

        exposureNotificationClient.getDailySummaries(DailySummariesConfig.DailySummariesConfigBuilder().apply {
            for (i in 0 .. 5){
                setReportTypeWeight(i, reportTypeWeights[i])
            }
            setAttenuationBuckets(attenuationBucketThresholdDb, attenuationBucketWeights)
            for (i in 0 .. 2){
                setInfectiousnessWeight(i, infectiousnessWeights[i])
            }
            setMinimumWindowScore(AppConfig.minimumWindowScore)
        }.build()).addOnSuccessListener {
            cont.resume(it)
        }.addOnFailureListener {
            cont.resumeWithException(it)
        }
    }

    suspend fun getLastRiskyExposure(): DailySummary? {
       return getDailySummaries().maxBy { it.daysSinceEpoch }
    }

    suspend fun getTemporaryExposureKeyHistory(): List<TemporaryExposureKey> =
        suspendCoroutine { cont ->
            exposureNotificationClient.temporaryExposureKeyHistory.addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }

    suspend fun getExposureWindows(): List<ExposureWindow> = suspendCoroutine { cont ->
        exposureNotificationClient.exposureWindows
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }
}