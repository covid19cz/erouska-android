package cz.covid19cz.erouska.net.model

import android.util.Base64
import cz.covid19cz.erouska.BuildConfig
import java.util.*

data class ExposureRequest(
    val temporaryExposureKeys: List<TemporaryExposureKeyDto>,
    val verificationPayload: String?,
    val hmackey: String?,
    val symptomOnsetInterval: Int?,
    val revisionToken: String?,
    val padding: String = Base64.encodeToString(UUID.randomUUID().toString().toByteArray(), Base64.DEFAULT),
    val platform: String = "Android",
    val regions: List<String> = listOf("CZ"),
    val healthAuthority: String = BuildConfig.APPLICATION_ID
)

data class TemporaryExposureKeyDto(
    val key: String,
    val rollingStartNumber: Int,
    val rollingPeriod: Int,
    val transmissionRisk: Int
)

data class ExposureResponse(
    val revisionToken: String?,
    val insertedExposures: Int?,
    val errorMessage: String?,
    val code: String?
)