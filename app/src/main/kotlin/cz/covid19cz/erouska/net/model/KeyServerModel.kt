package cz.covid19cz.erouska.net.model

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