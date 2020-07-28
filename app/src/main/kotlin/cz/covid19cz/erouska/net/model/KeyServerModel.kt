package cz.covid19cz.erouska.net.model

import cz.covid19cz.erouska.BuildConfig

data class ExposureRequest(
    val temporaryExposureKeys: List<TemporaryExposureKey>,
    val verificationPayload: String,
    val hmackey: String,
    val symptomOnsetInterval: Int,
    val revisionToken: String,
    val padding: String,
    val deviceVerificationPayload: String,
    val platform: String = "Android",
    val regions: List<String> = listOf("CZ"),
    val appPackageName: String = BuildConfig.APPLICATION_ID
)

data class TemporaryExposureKey(
    val key: String,
    val rollingStartNumber: Int,
    val rollingPeriod: Int,
    val transmissionRisk: Int
)