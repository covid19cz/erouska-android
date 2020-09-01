package cz.covid19cz.erouska.net

import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import cz.covid19cz.erouska.AppConfig.FIREBASE_REGION
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.model.CovidStatsResponse
import cz.covid19cz.erouska.utils.DeviceInfo
import cz.covid19cz.erouska.utils.LocaleUtils
import kotlinx.coroutines.tasks.await

class FirebaseFunctionsRepository(
    private val deviceInfo: DeviceInfo,
    private val prefsRepository: SharedPrefsRepository
) {

    suspend fun registerEhrid() {
        val data = hashMapOf(
            "platform" to "android",
            "platformVersion" to deviceInfo.getAndroidVersion(),
            "manufacturer" to deviceInfo.getManufacturer(),
            "model" to deviceInfo.getDeviceName(),
            "locale" to LocaleUtils.getLocale()
        )
        val ehrid = checkNotNull(callFunction("RegisterEhrid", data)["ehrid"])
        prefsRepository.saveEhrid(ehrid)
    }

    suspend fun getStats(date: String? = null): CovidStatsResponse {
        val data = hashMapOf(
            "date" to date
        )
        val covidStats = callFunction("GetCovidData", data)
        return Gson().fromJson(covidStats.toString(), CovidStatsResponse::class.java)
    }

    private suspend fun callFunction(
        name: String,
        data: Map<String, String?>? = null
    ): Map<String, String> {
        @Suppress("UNCHECKED_CAST")
        return Firebase.functions(FIREBASE_REGION).getHttpsCallable(name).call(data)
            .await().data as Map<String, String>
    }
}