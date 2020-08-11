package cz.covid19cz.erouska.net

import android.content.Context
import android.util.Base64
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.gson.GsonBuilder
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.net.api.KeyServerApi
import cz.covid19cz.erouska.net.api.VerificationServerApi
import cz.covid19cz.erouska.net.model.*
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.io.inputstream.ZipInputStream
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.random.Random


class ExposureServerRepository(private val context: Context) {

    private val okhttpBuilder by lazy {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
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
                    .addHeader("X-API-Key", context.getString(R.string.verification_server_api_key))
                    .build()
                it.proceed(request)
            }.build())
            .build().create(VerificationServerApi::class.java)
    }

    suspend fun reportExposure(request: ExposureRequest): ExposureResponse {
        return withContext(Dispatchers.IO) {
            keyServerClient.reportExposure(request)
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

    suspend fun downloadKeyExport(): List<File> {
        return withContext(Dispatchers.IO) {
            val connection = URL(context.getString(R.string.key_export_url)).openConnection()
            val inputStream = connection.getInputStream()
            val readBuffer = ByteArray(4096)
            val zipStream = ZipInputStream(inputStream)
            val extractedDir = File(context.cacheDir.path + "/export/" + System.currentTimeMillis())
            extractedDir.mkdirs()
            val extractedFiles = mutableListOf<File>()
            do {
                val zipEntry = zipStream.nextEntry
                if (zipEntry != null) {
                    val extractedFile = File(extractedDir.path + "/" + zipEntry.fileName)
                    extractedFile.createNewFile()
                    val outputStream = FileOutputStream(extractedFile)
                    do {
                        val readLen = zipStream.read(readBuffer)
                        if (readLen == -1) {
                            outputStream.close()
                            extractedFiles.add(extractedFile)
                        } else {
                            outputStream.write(readBuffer, 0, readLen)
                        }
                    } while (readLen != -1)
                } else {
                    zipStream.close()
                }
            } while (zipEntry != null)
            extractedFiles
        }
    }

    private fun hashedKeys(keys : List<TemporaryExposureKey>) : String? {
        val cleartextSegments = ArrayList<String>()
        for (k in keys) {
            cleartextSegments.add(String.format(
                    Locale.ENGLISH,
                    "%s.%d.%d",
                    Base64.encodeToString(k.keyData, Base64.DEFAULT),
                    k.rollingStartIntervalNumber,
                    k.rollingPeriod))
        }
        val cleartext = cleartextSegments.joinToString(",")
        L.d( "${keys.size} keys for hashing prior to verification: [" + cleartext + "]")
        try {
            val mac = Mac.getInstance("HmacSHA256");
            mac.init(SecretKeySpec(Base64.decode(newHmacKey(), Base64.DEFAULT), "HmacSHA256"))
            return Base64.encodeToString(mac.doFinal(cleartext.toByteArray(StandardCharsets.UTF_8)), Base64.DEFAULT)
        } catch (t : Throwable) {
            L.e(t)
        }
        return null
    }

    fun newHmacKey(): String? {
        val bytes = ByteArray(16)
        Random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

}