package cz.covid19cz.erouska.net

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.worker.DownloadKeysWorker
import cz.covid19cz.erouska.net.api.KeyServerApi
import cz.covid19cz.erouska.net.api.VerificationServerApi
import cz.covid19cz.erouska.net.model.*
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.reflect.Type
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureServerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SharedPrefsRepository
) {

    private val okhttpBuilder by lazy {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(UserAgentInterceptor())
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        builder
    }

    private val keyServerClient by lazy {
        Retrofit.Builder()
            .baseUrl(context.getString(R.string.key_server_base_url))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(okhttpBuilder.build())
            .build().create(KeyServerApi::class.java)
    }

    private val verificationServerClient by lazy {
        Retrofit.Builder()
            .baseUrl(context.getString(R.string.verification_server_base_url))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(okhttpBuilder.addInterceptor {
                val request = it.request().newBuilder()
                    .addHeader("X-API-Key", AppConfig.verificationServerApiKey)
                    .build()
                it.proceed(request)
            }.build())
            .build().create(VerificationServerApi::class.java)
    }

    suspend fun reportExposure(
        temporaryExposureKeyDto: List<TemporaryExposureKeyDto>,
        certificate: String,
        hmackey: String
    ): ExposureResponse {
        return withContext(Dispatchers.IO) {
            keyServerClient.reportExposure(
                ExposureRequest(
                    temporaryExposureKeys = temporaryExposureKeyDto,
                    verificationPayload = certificate,
                    hmackey = hmackey,
                    revisionToken = null,
                    traveler = prefs.isTraveller(),
                    consentToFederation = prefs.isConsentToFederation(),
                    reportType = AppConfig.efgsReportType,
                    visitedCountries = if (prefs.isTraveller()) AppConfig.efgsVisitedCountries else emptyList(),
                    symptomOnsetInterval = prefs.getSymptomOnsetInterval()
                )
            )
        }
    }

    suspend fun verifyCode(request: VerifyCodeRequest): VerifyCodeResponse {
        return withContext(Dispatchers.IO) {
            verificationServerClient.verifyCode(request)
        }
    }

    suspend fun verifyCertificate(request: VerifyCertificateRequest): VerifyCertificateResponse {
        return withContext(Dispatchers.IO) {
            verificationServerClient.verifyCertificate(request)
        }
    }

    suspend fun cover(request: CoverRequest): CoverResponse {
        return withContext(Dispatchers.IO) {
            verificationServerClient.cover(request)
        }
    }

    suspend fun downloadKeyExport(): List<DownloadedKeys> {
        return withContext(Dispatchers.IO) {
            val countryUrls = parseCountryUrls(
                if (prefs.isTraveller()) {
                    AppConfig.keyExportEuTravellerUrls
                } else {
                    AppConfig.keyExportNonTravellerUrls
                }
            )
            val keysList = mutableListOf<DownloadedKeys>()
            val keysListTasks = mutableListOf<Deferred<DownloadedKeys?>>()
            countryUrls.forEach {
                keysListTasks.add(async { downloadIndex(it) })
            }
            keysList.addAll(keysListTasks.awaitAll().filterNotNull())
            return@withContext keysList
        }
    }

    private fun parseCountryUrls(json: String): List<String> {
        val countryUrlListType: Type = object : TypeToken<ArrayList<CountryUrl>?>() {}.type
        val countryUrls: ArrayList<CountryUrl> = Gson().fromJson(json, countryUrlListType)
        return countryUrls.map { it.url }
    }

    private suspend fun downloadIndex(url: String): DownloadedKeys? {
        return withContext(Dispatchers.IO) {
            val indexContent = readURLContent(url)
            if (indexContent != null) {
                val lastDownloadedFile = prefs.lastKeyExportFileName(url)
                var fileNames = indexContent.split('\n')

                // Find index of last downloaded file and get everything after it
                val indexOfLastDownload = fileNames.indexOf(lastDownloadedFile)
                if (indexOfLastDownload != -1) {
                    fileNames = fileNames.subList(indexOfLastDownload + 1, fileNames.size)
                }

                val extractedFiles = mutableListOf<File>()
                val zipUrls = fileNames.map { AppConfig.keyExportUrl + it }

                val downloads = mutableListOf<Deferred<File?>>()
                zipUrls.forEach {
                    downloads.add(async { downloadFile(it) })
                }
                extractedFiles.addAll(downloads.awaitAll().filterNotNull())
                return@withContext DownloadedKeys(url, extractedFiles, fileNames)
            } else {
                return@withContext null
            }
        }
    }

    private fun downloadFile(zipfile: String): File? {
        try {
            val dir = File(context.cacheDir.path + "/export/")
            val fileName = if (zipfile.contains("efgs_")) {
                // EFGS files; e.g. efgs_de/1607061600-1607068800-00001.zip
                zipfile.split("/").takeLast(2).joinToString("/")
            } else {
                // CZ files; e.g. 1607061600-1607068800-00001.zip
                zipfile.substring(zipfile.lastIndexOf("/") + 1)
            }
            val file = File(dir.absolutePath + "/" + fileName)

            checkNotNull(file.parentFile).mkdirs()

            file.createNewFile()
            val u = URL(zipfile)
            val inputStream: InputStream = u.openStream()
            val dis = DataInputStream(inputStream)
            val buffer = ByteArray(1024)
            var length: Int
            val fos = FileOutputStream(file)
            while (dis.read(buffer).also { length = it } > 0) {
                fos.write(buffer, 0, length)
            }
            return file
        } catch (t: Throwable) {
            L.e(t)
        }
        return null
    }

    private fun readURLContent(url: String): String? {
        return try {
            val indexConnection = URL(url).openConnection()
            val indexInputStream = indexConnection.getInputStream()

            indexInputStream.readBytes().toString(Charsets.UTF_8)
        } catch (e: Throwable) {
            L.w("Skipping index download due to $e")
            null
        }
    }

    fun scheduleKeyDownload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val worker = PeriodicWorkRequestBuilder<DownloadKeysWorker>(
            AppConfig.keyImportPeriodHours,
            TimeUnit.HOURS
        ).setConstraints(constraints)
            .addTag(DownloadKeysWorker.TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DownloadKeysWorker.TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                worker
            )
    }

    fun deleteFiles() {
        val extractedDir = File(context.cacheDir.path + "/export/")
        extractedDir.deleteRecursively()
        prefs.clearLastKeyExportFileName()
        prefs.clearLastKeyImportTime()
    }

    class UserAgentInterceptor : Interceptor {

        companion object {
            const val USER_AGENT = "User-agent";
        }

        private val userAgent =
            "eRouska-Android-${BuildConfig.BUILD_TYPE}/${BuildConfig.VERSION_NAME}"

        override fun intercept(chain: Interceptor.Chain): Response {
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader(USER_AGENT, userAgent)
            return chain.proceed(requestBuilder.build())
        }

    }
}