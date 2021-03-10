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
import java.io.File
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
                prefs.setExposureNotificationsEnabled(true)
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun stop() = suspendCoroutine<Void> { cont ->
        client.stop()
            .addOnSuccessListener {
                prefs.setExposureNotificationsEnabled(false)
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

    suspend fun getStatus(): Set<ExposureNotificationStatus> = suspendCoroutine { cont ->
        client.status
            .addOnSuccessListener {
                cont.resume(it)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    suspend fun provideDiagnosisKeys(
        keyList: List<DownloadedKeys>
    ): Boolean = suspendCoroutine { cont ->

        setDiagnosisKeysMapping()

        val filesToImport = mutableListOf<File>()
        keyList.forEach { keys ->
            if (keys.isValid()) {
                if (keys.files.isNotEmpty()) {
                    L.i("Importing keys ${keys.indexUrl}")
                    filesToImport.addAll(keys.files)
                } else {
                    L.i("Import skipped (no new data) ${keys.indexUrl}")
                }
            } else {
                L.i("Import skipped (invalid data) ${keys.indexUrl}")
            }
        }

        if (filesToImport.isEmpty()) {
            L.i("All skipped (empty)")
            prefs.setLastKeyImport()
        } else {
            client.provideDiagnosisKeys(filesToImport)
                .addOnSuccessListener {
                    L.i("Import success of ${filesToImport.size} files")
                    prefs.setLastKeyImport()
                    keyList.forEach { keys ->
                        if (keys.isValid() && keys.files.isNotEmpty()) {
                            L.d("Last successful import for ${keys.indexUrl} is ${keys.getLastUrl()}")
                            prefs.setLastKeyExportFileName(keys.indexUrl, keys.getLastUrl())
                        }
                    }
                    cont.resume(true)
                }.addOnFailureListener {
                    cont.resumeWithException(it)
                }
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
                prefs.getAttenuationBucketThresholdDb()
                    ?: AppConfig.attenuationBucketThresholdDb
            val attenuationBucketWeights =
                prefs.getAttenuationBucketWeights() ?: AppConfig.attenuationBucketWeights
            val infectiousnessWeights =
                prefs.getInfectiousnessWeights() ?: AppConfig.infectiousnessWeights

            client.getDailySummaries(
                DailySummariesConfig.DailySummariesConfigBuilder().apply {

                    setReportTypeWeight(ReportType.CONFIRMED_TEST, reportTypeWeights[1])
                    setReportTypeWeight(
                        ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,
                        reportTypeWeights[2]
                    )
                    setReportTypeWeight(ReportType.SELF_REPORT, reportTypeWeights[3])
                    setReportTypeWeight(ReportType.RECURSIVE, reportTypeWeights[4])

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

                val dtos = keys.map {
                    TemporaryExposureKeyDto(
                        it.keyData.encodeBase64(),
                        it.rollingStartIntervalNumber,
                        it.rollingPeriod
                    )
                }
                L.i("Uploading ${dtos.size} keys")
                val response = server.reportExposure(dtos, certificateResponse.certificate, hmackey)
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

    suspend fun checkExposure() {
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

        // latest exposure found in the database
        val latestExposure = db.dao().getLatest().firstOrNull()
        val latestExposureTime = latestExposure?.daysSinceEpoch

        // latest exposure that the user was not notified about yet
        val lastNotifiedExposureTime = db.dao().getLastNotified().firstOrNull()?.daysSinceEpoch

        // the app should show a notification if there is a new exposure the user was not notified
        // about, yet, or if there is an exposure, but the app has not been opened since the last
        // last notification
        val userNotNotifiedAboutLatest = latestExposureTime != null
                && latestExposureTime != lastNotifiedExposureTime
        val lastAppUsedTimestamp = prefs.getLastTimeAppVisited()
        // Suppress showing update screen after launching the app for first time with a new exposure.
        prefs.setSuppressUpdateScreens(true)
        // We can use the import timestamp of the exposure as we are interested in comparing whether
        // the user visited the app after being notified. The notification can take place only when
        // the exposure is imported.
        // In case there is no exposure, the timestamp will default to 0.
        // It won't cause a false positive as the app timestamp will always be greater than 0.
        val lastExposureTimestamp = latestExposure?.importTimestamp ?: 0L
        // If the app visit timestamp is not saved yet, it acts as if the user has not opened the app.
        // To reduce false positives, we should check the timestamp is non-zero.
        val appNotOpenedSinceLastNotification = (lastAppUsedTimestamp > 0)
                && (lastExposureTimestamp > lastAppUsedTimestamp)

        val shouldNotify = userNotNotifiedAboutLatest || appNotOpenedSinceLastNotification

        if (shouldNotify) {
            notifications.showRiskyExposureNotification()
            db.dao().markAsNotified()
            firebaseFunctionsRepository.registerNotification()
        } else {
            L.i(
                "Not showing notification, lastExposure=$latestExposureTime, " +
                        "lastNotifiedExposure=$lastNotifiedExposureTime"
            )
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

    fun isLocationlessScanSupported() = client.deviceSupportsLocationlessScanning()

}