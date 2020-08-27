package cz.covid19cz.erouska.net.model

// Taken from https://github.com/google/exposure-notifications-verification-server#api-guide-for-app-developers

data class VerifyCodeRequest(
    val code: String
)

data class VerifyCodeResponse(
    val testType: String?,
    val symptomDate: String?,
    val token: String?,
    val error: String?
)

data class VerifyCertificateRequest(
    val token: String,
    val ekeyhmac: String
)

data class VerifyCertificateResponse(
    val certificate: String,
    val error: String?
)

data class CoverRequest(
    val data: String
)

data class CoverResponse(
    val data: String,
    val error: String?
)