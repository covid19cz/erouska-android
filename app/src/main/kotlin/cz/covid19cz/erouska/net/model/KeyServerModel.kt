package cz.covid19cz.erouska.net.model

import android.util.Base64
import java.util.*

data class ExposureRequest(
    val temporaryExposureKeys: List<TemporaryExposureKeyDto>,
    val verificationPayload: String?,
    val hmackey: String?,
    val revisionToken: String?,
    val traveler: Boolean,
    val consentToFederation: Boolean,
    val reportType: String,
    val visitedCountries: List<String>,
    val padding: String = Base64.encodeToString(UUID.randomUUID().toString().toByteArray(), Base64.NO_WRAP),
    val healthAuthorityID: String = "cz.covid19cz.erouska",
)

data class TemporaryExposureKeyDto(
    val key: String,
    val rollingStartNumber: Int,
    val rollingPeriod: Int
)

data class ExposureResponse(
    val revisionToken: String?,
    val insertedExposures: Int?,
    val errorMessage: String?,
    val code: String?
)