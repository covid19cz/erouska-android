package cz.covid19cz.erouska.net.api

import cz.covid19cz.erouska.net.model.CovidStatsRequest
import cz.covid19cz.erouska.net.model.CovidStatsResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface CovidDataServerApi {

    @POST("GetCovidData")
    suspend fun getStats(@Body covidStatsRequest: CovidStatsRequest) : CovidStatsResponse
}