package cz.covid19cz.erouska.net.api

import cz.covid19cz.erouska.net.model.ExposureRequest
import cz.covid19cz.erouska.net.model.ExposureResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface KeyServerApi {

    @POST("PublishKeys")
    suspend fun reportExposure(@Body exposureRequest: ExposureRequest) : ExposureResponse
}