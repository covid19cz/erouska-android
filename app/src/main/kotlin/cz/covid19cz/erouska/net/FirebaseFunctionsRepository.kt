package cz.covid19cz.erouska.net

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import cz.covid19cz.erouska.AppConfig.FIREBASE_REGION
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.exception.UnauthrorizedException
import cz.covid19cz.erouska.net.model.CovidStatsResponse
import cz.covid19cz.erouska.utils.DeviceInfo
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.LocaleUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

@Singleton
class FirebaseFunctionsRepository @Inject constructor(
    private val deviceInfo: DeviceInfo,
    private val prefs: SharedPrefsRepository
) {

    /**
     * Creates a new registration, saved to registrations collection.
     */
    suspend fun register(pushRegistrationToken: String) {
        withContext(Dispatchers.IO) {
            val data = hashMapOf(
                "platform" to "android",
                "platformVersion" to deviceInfo.getAndroidVersion(),
                "manufacturer" to deviceInfo.getManufacturer(),
                "model" to deviceInfo.getDeviceName(),
                "locale" to LocaleUtils.getLocale(),
                "pushRegistrationToken" to pushRegistrationToken
            )
            val token = checkNotNull(callFunction("RegisterEhrid", data)["customToken"])
            FirebaseAuth.getInstance().signInWithCustomToken(token).await()
            if (FirebaseAuth.getInstance().currentUser == null) {
                throw RuntimeException("Sign in failed")
            }
            prefs.setPushTokenRegistered()
        }
    }

    /**
     * Returns data from collections covidDataIncrease and covidDataTotal for the given input date (if date is missing, TODAY is used)
     */
    suspend fun getStats(date: String? = null): CovidStatsResponse {
        return withContext(Dispatchers.IO) {
            val data = hashMapOf(
                "idToken" to getIdToken(),
                "date" to date
            )
            val covidStats = callFunction("GetCovidData", data)
            Gson().fromJson(covidStats.toString(), CovidStatsResponse::class.java)
        }
    }

    /**
     * In the registrations collection, changes the value of lastNotificationStatus attribute to sent and the value of lastNotificationUpdatedAt attribute to CURRENT_TIMESTAMP for a given idToken.
     */
    suspend fun registerNotification() {
        withContext(Dispatchers.IO) {
            val data = hashMapOf(
                "idToken" to getIdToken()
            )
            callFunction("RegisterNotification", data)
        }
    }

    /**
     * Changes push token
     */
    suspend fun changePushToken(pushRegistrationToken: String) {
        withContext(Dispatchers.IO) {
            val data = hashMapOf(
                "idToken" to getIdToken(),
                "pushRegistrationToken" to pushRegistrationToken
            )
            callFunction("ChangePushToken", data)
            prefs.setPushTokenRegistered()
        }
    }

    private suspend fun getIdToken(): String = suspendCoroutine { cont ->
        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.addOnSuccessListener {
                cont.resumeWith(Result.success(checkNotNull(it.token)))
            }?.addOnFailureListener {
                cont.resumeWith(Result.failure(it))
            }
        } else {
            cont.resumeWith(Result.failure(UnauthrorizedException()))
        }
    }

    /**
     * Generic function for calling Firebase Function.
     */
    private suspend fun callFunction(
        name: String,
        data: Map<String, String?>? = null
    ): Map<String, String> {
        L.d("Calling function $name with data: $data")
        @Suppress("UNCHECKED_CAST")
        val response = Firebase.functions(FIREBASE_REGION).getHttpsCallable(name).call(data)
            .continueWith { task ->
                (task.result?.data as HashMap<String, String>)
            }.await()
        L.d("Function $name response: $response")
        return response
    }
}