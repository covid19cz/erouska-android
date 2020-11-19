package cz.covid19cz.erouska.exposurenotifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.nearby.exposurenotification.*
import com.google.gson.Gson
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.DailySummariesDb
import cz.covid19cz.erouska.db.DailySummaryEntity
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.worker.SelfCheckerWorker
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.net.model.*
import cz.covid19cz.erouska.ui.senddata.NoKeysException
import cz.covid19cz.erouska.ui.senddata.ReportExposureException
import cz.covid19cz.erouska.ui.senddata.VerifyException
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.qualifiers.ApplicationContext
import org.threeten.bp.LocalDate
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class ExposureNotificationsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: ExposureNotificationClient,
    private val server: ExposureServerRepository,
    private val cryptoTools: ExposureCryptoTools,
    private val prefs: SharedPrefsRepository,
    private val firebaseFunctionsRepository: FirebaseFunctionsRepository,
    private val db: DailySummariesDb,
    private val notifications: Notifications
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
        keys: DownloadedKeys
    ): Boolean = suspendCoroutine { cont ->

        setDiagnosisKeysMapping()

        if (keys.isValid()) {
            if (keys.files.isNotEmpty()) {
                L.i("Importing keys")
                client.provideDiagnosisKeys(keys.files)
                    .addOnSuccessListener {
                        L.i("Import success")
                        prefs.setLastKeyImport()

                        prefs.setLastKeyExportFileName(keys.getLastUrl())
                        cont.resume(true)
                    }.addOnFailureListener {
                        cont.resumeWithException(it)
                    }
            } else {
                L.i("Import skipped (no new data)")
                prefs.setLastKeyImport()
                cont.resume(true)
            }
        } else {
            L.i("Import skipped (invalid data)")
            cont.resume(true)
        }
    }

    private fun setDiagnosisKeysMapping() {
        if (System.currentTimeMillis() - prefs.getLastSetDiagnosisKeysDataMapping() > AppConfig.diagnosisKeysDataMappingLimitDays * 24 * 60 * 60 * 1000) {
            val daysList = AppConfig.daysSinceOnsetToInfectiousness
            val daysToInfectiousness = mutableMapOf<Int, Int>()
            for (i in -14..14) {
                daysToInfectiousness[i] = daysList[i + 14]
            }
            val mapping = DiagnosisKeysDataMapping.DiagnosisKeysDataMappingBuilder()
                .setDaysSinceOnsetToInfectiousness(daysToInfectiousness)
                .setInfectiousnessWhenDaysSinceOnsetMissing(AppConfig.infectiousnessWhenDaysSinceOnsetMissing)
                .setReportTypeWhenMissing(AppConfig.reportTypeWhenMissing)
                .build()
            try {
                client.setDiagnosisKeysDataMapping(mapping)
            } catch (t: Throwable) {
                L.e(t)
            } finally {
                prefs.setLastSetDiagnosisKeysDataMapping()
            }
        }
    }

    suspend fun getDailySummariesFromApi(filter: Boolean = true): List<DailySummary> =
        suspendCoroutine { cont ->

            val reportTypeWeights = prefs.getReportTypeWeights() ?: AppConfig.reportTypeWeights
            val attenuationBucketThresholdDb =
                prefs.getAttenuationBucketThresholdDb() ?: AppConfig.attenuationBucketThresholdDb
            val attenuationBucketWeights =
                prefs.getAttenuationBucketWeights() ?: AppConfig.attenuationBucketWeights
            val infectiousnessWeights =
                prefs.getInfectiousnessWeights() ?: AppConfig.infectiousnessWeights

            client.getDailySummaries(
                DailySummariesConfig.DailySummariesConfigBuilder().apply {

                    setReportTypeWeight(ReportType.CONFIRMED_TEST, reportTypeWeights[0])
                    setReportTypeWeight(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, reportTypeWeights[1])
                    setReportTypeWeight(ReportType.SELF_REPORT, reportTypeWeights[2])
                    setReportTypeWeight(ReportType.RECURSIVE, reportTypeWeights[3])

                    setInfectiousnessWeight(Infectiousness.NONE, infectiousnessWeights[0])
                    setInfectiousnessWeight(Infectiousness.STANDARD, infectiousnessWeights[1])
                    setInfectiousnessWeight(Infectiousness.HIGH, infectiousnessWeights[2])

                    setAttenuationBuckets(attenuationBucketThresholdDb, attenuationBucketWeights)
                    setMinimumWindowScore(AppConfig.minimumWindowScore)
                }.build()
            ).addOnSuccessListener {
                if (filter) {
                    cont.resume(it.filter {
                        it.summaryData.maximumScore >= AppConfig.minimumWindowScore
                    })
                } else {
                    cont.resume(it)
                }

            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
        }

    suspend fun getDailySummariesFromDbByExposureDate(): List<DailySummaryEntity> {
        return db.dao().getAllByExposureDate()
    }

    suspend fun getDailySummariesFromDbByImportDate(): List<DailySummaryEntity> {
        return db.dao().getAllByImportDate()
    }

    suspend fun getLastRiskyExposure(demo: Boolean? = false): DailySummaryEntity? {
        val lastExposure = db.dao().getLatest().firstOrNull()
        return if (lastExposure == null && demo == true) {
            getLastRiskyExposureForDemo()
        } else {
            lastExposure
        }
    }

    private fun getLastRiskyExposureForDemo(): DailySummaryEntity {
        return DailySummaryEntity(
            LocalDate.now().minusDays(1).toEpochDay().toInt(),
            1000.0,
            1000.0,
            1000.0,
            0,
            notified = false,
            accepted = false
        )
    }

    suspend fun markAsAccepted() {
        db.dao().markAsAccepted()
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

    suspend fun reportExposureWithVerification(code: String): Int {
        val keys = getTemporaryExposureKeyHistory()
        if (keys.isEmpty()) {
            L.e("No keys found, upload cancelled")
            throw NoKeysException()
        }
        try {
            val verifyResponse = server.verifyCode(VerifyCodeRequest(code))
            if (verifyResponse.token != null) {
                L.i("Verify code success")
                val hmackey = cryptoTools.newHmacKey()
                val keyHash = cryptoTools.hashedKeys(keys, hmackey)
                val token = verifyResponse.token

                val certificateResponse = server.verifyCertificate(
                    VerifyCertificateRequest(token, keyHash)
                )
                if (certificateResponse.error != null) {
                    // We ignore error in certificate verification, only log it. It was causing error in production builds with older server.
                    L.e("Error in certificate verification: " + certificateResponse.error + " (" + certificateResponse.errorCode + ")")
                } else {
                    L.i("Verify certificate success")
                }

                val request = ExposureRequest(
                    keys.map {
                        TemporaryExposureKeyDto(
                            it.keyData.encodeBase64(),
                            it.rollingStartIntervalNumber,
                            it.rollingPeriod
                        )
                    },
                    certificateResponse.certificate,
                    hmackey,
                    null,
                    null,
                    healthAuthorityID = "cz.covid19cz.erouska"
                )
                L.i("Uploading ${request.temporaryExposureKeys.size} keys")
                val response = server.reportExposure(request)
                response.errorMessage?.let {
                    L.e("Report exposure failed: $it")
                    throw ReportExposureException(it, response.code)
                }
                L.i("Report exposure success, ${response.insertedExposures} keys inserted")
                return response.insertedExposures ?: 0
            } else {
                throw VerifyException(verifyResponse.error, verifyResponse.errorCode)
            }
        } catch (e: HttpException) {
            var errorResponse: VerifyCodeResponse? = null
            try {
                val errorBody = e.response()?.errorBody()?.string()
                errorResponse =
                    Gson().fromJson<VerifyCodeResponse>(errorBody, VerifyCodeResponse::class.java)
            } catch (e: Throwable) {
                L.e(e)
            }
            // called when we have HTTP not 200
            if (e.code() == 500 && AppConfig.handleError500AsInvalidCode) {
                // This should be enabled only on the old prod server
                throw VerifyException("Invalid code", VerifyCodeResponse.ERROR_CODE_INVALID_CODE)
            } else if (e.code() == 400) {
                if (errorResponse?.errorCode == VerifyCodeResponse.ERROR_CODE_INVALID_CODE || errorResponse?.errorCode == VerifyCodeResponse.ERROR_CODE_EXPIRED_CODE) {
                    throw VerifyException(errorResponse.error, errorResponse.errorCode)
                } else if (AppConfig.handleError400AsExpiredOrUsedCode) {
                    throw VerifyException(
                        errorResponse?.error,
                        VerifyCodeResponse.ERROR_CODE_EXPIRED_USED_CODE
                    )
                } else {
                    throw VerifyException(errorResponse?.error, errorResponse?.errorCode)
                }
            } else {
                throw VerifyException(errorResponse?.error, errorResponse?.errorCode)
            }
        }
    }

    suspend fun checkExposure(context: Context) {
        db.dao().deleteOld()
        val timestamp = System.currentTimeMillis()
        db.dao().insert(getDailySummariesFromApi().map {
            DailySummaryEntity(
                daysSinceEpoch = it.daysSinceEpoch,
                maximumScore = it.summaryData.maximumScore,
                scoreSum = it.summaryData.scoreSum,
                weightenedDurationSum = it.summaryData.weightedDurationSum,
                importTimestamp = timestamp,
                notified = false,
                accepted = false
            )
        })
        val latestExposure = db.dao().getLatest().firstOrNull()?.daysSinceEpoch
        val lastNotifiedExposure = db.dao().getLastNotified().firstOrNull()?.daysSinceEpoch
        if (latestExposure != null && latestExposure != lastNotifiedExposure) {
            notifications.showRiskyExposureNotification()
            db.dao().markAsNotified()
            firebaseFunctionsRepository.registerNotification()
        } else {
            L.i("Not showing notification, lastExposure=$latestExposure, lastNotifiedExposure=$lastNotifiedExposure")
        }
    }

    //TODO: Remove in late november 2020
    suspend fun importLegacyExposures() {
        if (!prefs.isLegacyExposuresImported()) {
            db.dao().insert(getDailySummariesFromApi(filter = false).map {
                DailySummaryEntity(
                    daysSinceEpoch = it.daysSinceEpoch,
                    maximumScore = it.summaryData.maximumScore,
                    scoreSum = it.summaryData.scoreSum,
                    weightenedDurationSum = it.summaryData.weightedDurationSum,
                    importTimestamp = if (it.daysSinceEpoch > prefs.getLastNotifiedExposure()) System.currentTimeMillis() else 0,
                    notified = it.daysSinceEpoch <= prefs.getLastNotifiedExposure(),
                    accepted = it.daysSinceEpoch <= prefs.getLastInAppNotifiedExposure()
                )
            })
            prefs.cleanLegacyExposurePrefs()
            prefs.setLegacyExposuresImported()
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

    suspend fun isEligibleToDownloadKeys(): Boolean {
        return isEnabled() && System.currentTimeMillis() - prefs.getLastKeyImport() >= AppConfig.keyImportPeriodHours * 60 * 60 * 1000
    }
}