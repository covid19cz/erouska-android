package cz.covid19cz.erouska.net.api

import cz.covid19cz.erouska.net.model.ExposureRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface KeyServerApi {

    @POST
    suspend fun reportExposure(@Body exposureRequest: ExposureRequest)
}