package cz.covid19cz.erouska.net.api

import cz.covid19cz.erouska.net.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface VerificationServerApi {

    @POST("verify")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): VerifyCodeResponse

    @POST("certificate")
    suspend fun verifyCertificate(@Body request: VerifyCertificateRequest): VerifyCertificateResponse

    @POST("cover")
    suspend fun cover(@Body request: CoverRequest): CoverResponse
}