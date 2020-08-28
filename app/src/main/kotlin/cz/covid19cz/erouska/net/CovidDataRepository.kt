package cz.covid19cz.erouska.net

import android.content.Context
import com.google.gson.GsonBuilder
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.net.api.CovidDataServerApi
import cz.covid19cz.erouska.net.model.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class CovidDataRepository(
    private val context: Context
) {

    private val okhttpBuilder by lazy {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        builder
    }

    private val covidDataClient by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.covidDataServerUrl)
            .addConverterFactory(CustomConverterFactory(GsonBuilder().create()))
            .client(okhttpBuilder.addInterceptor {
                val request = it.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                it.proceed(request)
            }.build())
            .build().create(CovidDataServerApi::class.java)
    }


    suspend fun getStats(request: CovidStatsRequest): CovidStatsResponse {
        return withContext(Dispatchers.IO) {
            covidDataClient.getStats(request)
        }
    }
}

