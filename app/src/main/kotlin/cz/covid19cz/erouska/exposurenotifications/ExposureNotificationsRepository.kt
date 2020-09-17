package cz.covid19cz.erouska.exposurenotifications

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Base64
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.nearby.exposurenotification.*
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.worker.SelfCheckerWorker
import cz.covid19cz.erouska.ext.isLocationEnabled
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.model.ExposureRequest
import cz.covid19cz.erouska.net.model.TemporaryExposureKeyDto
import cz.covid19cz.erouska.net.model.VerifyCertificateRequest
import cz.covid19cz.erouska.net.model.VerifyCodeRequest
import cz.covid19cz.erouska.ui.senddata.ReportExposureException
import cz.covid19cz.erouska.ui.senddata.VerifyException
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ExposureNotificationsRepository(
    private val context: Context,
    private val client: ExposureNotificationClient,
    private val server: ExposureServerRepository,
    private val cryptoTools: ExposureCryptoTools,
    private val prefs: SharedPrefsRepository
) {

    suspend fun start() = suspendCoroutine<Void> { cont ->
        client.start()
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun stop() = suspendCoroutine<Void> { cont ->
        client.stop()
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun isEnabled(): Boolean = suspendCoroutine { cont ->
        client.isEnabled
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun provideDiagnosisKeys(
        files: List<File>
    ): Void = suspendCoroutine { cont ->
        client.provideDiagnosisKeys(
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
        val attenuationBucketThresholdDb =
            prefs.getAttenuationBucketThresholdDb() ?: AppConfig.attenuationBucketThresholdDb
        val attenuationBucketWeights =
            prefs.getAttenuationBucketWeights() ?: AppConfig.attenuationBucketWeights
        val infectiousnessWeights =
            prefs.getInfectiousnessWeights() ?: AppConfig.infectiousnessWeights

        client.getDailySummaries(
            DailySummariesConfig.DailySummariesConfigBuilder().apply {
                for (i in 0..5) {
                    setReportTypeWeight(i, reportTypeWeights[i])
                }
                setAttenuationBuckets(attenuationBucketThresholdDb, attenuationBucketWeights)
                for (i in 0..2) {
                    setInfectiousnessWeight(i, infectiousnessWeights[i])
                }
                setMinimumWindowScore(AppConfig.minimumWindowScore)
            }.build()
        ).addOnSuccessListener {
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
            client.temporaryExposureKeyHistory.addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }

    suspend fun getExposureWindows(): List<ExposureWindow> = suspendCoroutine { cont ->
        client.exposureWindows
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun reportExposureWithoutVerification(): Int {
        val keys = getTemporaryExposureKeyHistory()
        val request = ExposureRequest(keys.map {
            TemporaryExposureKeyDto(
                Base64.encodeToString(
                    it.keyData,
                    Base64.NO_WRAP
                ), it.rollingStartIntervalNumber, it.rollingPeriod
            )
        }, null, null, null, prefs.getRevisionToken())
        val response = server.reportExposure(request)
        if (response.errorMessage != null) {
            throw ReportExposureException(response.errorMessage)
        }
        prefs.saveRevisionToken(response.revisionToken)
        return response.insertedExposures ?: 0
    }

    suspend fun reportExposureWithVerification(code: String): Int {
        val keys = getTemporaryExposureKeyHistory()
        val verifyResponse = server.verifyCode(VerifyCodeRequest(code))

        if (verifyResponse.token != null) {
            val hmackey = cryptoTools.newHmacKey()
            val keyHash = cryptoTools.hashedKeys(keys, hmackey)
            val token = verifyResponse.token

            val certificateResponse = server.verifyCertificate(
                VerifyCertificateRequest(token, keyHash)
            )

            val request = ExposureRequest(
                keys.map {
                    TemporaryExposureKeyDto(
                        Base64.encodeToString(
                            it.keyData,
                            Base64.NO_WRAP
                        ), it.rollingStartIntervalNumber, it.rollingPeriod
                    )
                },
                certificateResponse.certificate,
                hmackey,
                null,
                prefs.getRevisionToken(),
                healthAuthorityID = "cz.covid19cz.erouska"
            )
            val response = server.reportExposure(request)
            response.errorMessage?.let {
                throw ReportExposureException(it)
            }
            prefs.saveRevisionToken(response.revisionToken)
            return response.insertedExposures ?: 0
        } else {
            throw VerifyException(verifyResponse.error ?: "Unknown")
        }
    }

    fun scheduleSelfChecker() {
        val constraints = Constraints.Builder().build()
        val worker = PeriodicWorkRequestBuilder<SelfCheckerWorker>(
            AppConfig.selfCheckerPeriodHours,
            TimeUnit.HOURS
        ).setConstraints(constraints)
            .addTag(SelfCheckerWorker.TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                SelfCheckerWorker.TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                worker
            )
    }

    fun isEligibleToDownloadKeys() : Boolean{
        return System.currentTimeMillis() - prefs.getLastKeyImport() >= AppConfig.keyImportPeriodHours * 60 * 60 * 1000
    }
}