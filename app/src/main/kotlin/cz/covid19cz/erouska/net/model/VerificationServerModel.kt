package cz.covid19cz.erouska.net.model

// Taken from https://github.com/google/exposure-notifications-verification-server#api-guide-for-app-developers

data class VerifyCodeRequest(
    val code: String
)

data class VerifyCodeResponse(
    val TestType: String,
    val SymptomDate: String,
    val VerificationToken: String,
    val Error: String?
)

data class VerifyCertificateRequest(
    val VerificationToken: String,
    val ekeyhmac: String
)

data class VerifyCertificateResponse(
    val Certificate: String,
    val Error: String?
)

data class CoverRequest(
    val Data: String
)

data class CoverResponse(
    val Data: String,
    val Error: String?
)