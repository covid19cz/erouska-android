package cz.covid19cz.erouska.net

import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import cz.covid19cz.erouska.AppConfig.FIREBASE_REGION
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.model.CovidStatsResponse
import cz.covid19cz.erouska.utils.DeviceInfo
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.LocaleUtils
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class FirebaseFunctionsRepository(
    private val deviceInfo: DeviceInfo,
    private val prefsRepository: SharedPrefsRepository
) {

    /**
     * Creates a new registration, saved to registrations collection.
     */
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

    /**
     * Returns data from collections covidDataIncrease and covidDataTotal for the given input date (if date is missing, TODAY is used)
     */
    suspend fun getStats(date: String? = null): CovidStatsResponse {
        val data = hashMapOf(
            "date" to date
        )
        val covidStats = callFunction("GetCovidData", data)
        return Gson().fromJson(covidStats.toString(), CovidStatsResponse::class.java)
    }

    /**
     * In the registrations collection, changes the value of lastNotificationStatus attribute to sent and the value of lastNotificationUpdatedAt attribute to CURRENT_TIMESTAMP for a given eHRID.
     */
    suspend fun registerNotification(ehrid: String) {
        val data = hashMapOf(
            "ehrid" to ehrid
        )
        callFunction("registerNotification", data) {
            // TODO do some retry
        }
    }

    /**
     * Changes push token for eHRID.
     */
    suspend fun changePushToken(ehrid: String, pushRegistrationToken: String) {
        val data = hashMapOf(
            "ehrid" to ehrid,
            "pushRegistrationToken" to pushRegistrationToken
        )
        callFunction("changePushToken", data) {
            // TODO do some retry
        }
    }

    /**
     * Generic function for calling Firebase Function.
     */
    private suspend fun callFunction(
        name: String,
        data: Map<String, String?>? = null,
        onFailure: ((Exception) -> Unit)? = null
    ): Map<String, String> {
        @Suppress("UNCHECKED_CAST")
        return Firebase.functions(FIREBASE_REGION).getHttpsCallable(name).call(data)
            .continueWith { task ->
                (task.result?.data as HashMap<String, String>)
            }
            .addOnFailureListener { fail ->
                L.e("Firebase Function " + name + " failed. Reason : " + fail.localizedMessage)
                onFailure?.invoke(fail)
            }.await()
    }
}