package cz.covid19cz.erouska.exposurenotifications

import android.bluetooth.BluetoothAdapter
import android.util.Base64
import com.google.android.gms.nearby.exposurenotification.*
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.model.ExposureRequest
import cz.covid19cz.erouska.net.model.TemporaryExposureKeyDto
import cz.covid19cz.erouska.net.model.VerifyCertificateRequest
import cz.covid19cz.erouska.net.model.VerifyCodeRequest
import cz.covid19cz.erouska.ui.confirm.ReportExposureException
import cz.covid19cz.erouska.ui.confirm.VerifyException
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ExposureNotificationsRepository(
    private val client: ExposureNotificationClient,
    private val server: ExposureServerRepository,
    private val cryptoTools: ExposureCryptoTools,
    private val btAdapter: BluetoothAdapter,
    private val prefs: SharedPrefsRepository
) {

    private var keys: List<TemporaryExposureKey>? = null
    private var token: String? = null
    private var keyHash: String? = null
    private var hmackey: String? = null

    fun isBluetoothEnabled(): Boolean {
        return btAdapter.isEnabled
    }

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

    suspend fun reportExposureWithoutVerification() : Int {
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
        if (response.errorMessage != null){
            throw ReportExposureException(response.errorMessage)
        }
        prefs.saveRevisionToken(response.revisionToken)
        return response.insertedExposures ?: 0
    }

    suspend fun reportExposureWithVerification(code: String) : Int{
        if (token == null) {
            keys = getTemporaryExposureKeyHistory()
            val verifyResponse = server.verifyCode(VerifyCodeRequest(code))

            if (verifyResponse.token != null) {
                hmackey = cryptoTools.newHmacKey()
                keyHash = cryptoTools.hashedKeys(keys!!, hmackey!!)
                token = verifyResponse.token
            } else {
                throw VerifyException(verifyResponse.error ?: "Unknown")
            }
        }

        val certificateResponse = server.verifyCertificate(
            VerifyCertificateRequest(token!!, keyHash!!)
        )

        val request = ExposureRequest(
            keys!!.map {
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
            healthAuthorityID = if (BuildConfig.FLAVOR == "dev") {"cz.covid19cz.erouska.dev"} else {"cz.covid19cz.erouska"}
        )
        val response = server.reportExposure(request)
        response.errorMessage?.let {
            throw ReportExposureException(it)
        }
        prefs.saveRevisionToken(response.revisionToken)
        clearTempValues()
        return response.insertedExposures ?: 0
    }

    private fun clearTempValues(){
        token = null
        keys = null
        keyHash = null
        hmackey = null
    }
}