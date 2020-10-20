package cz.covid19cz.erouska.net

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import cz.covid19cz.erouska.AppConfig.FIREBASE_REGION
import cz.covid19cz.erouska.net.exception.UnauthrorizedException
import cz.covid19cz.erouska.net.model.CovidStatsResponse
import cz.covid19cz.erouska.utils.DeviceInfo
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.LocaleUtils
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

@Singleton
class FirebaseFunctionsRepository @Inject constructor(
    private val deviceInfo: DeviceInfo
) {

    /**
     * Creates a new registration, saved to registrations collection.
     */
    suspend fun register() : Boolean {
        val data = hashMapOf(
            "platform" to "android",
            "platformVersion" to deviceInfo.getAndroidVersion(),
            "manufacturer" to deviceInfo.getManufacturer(),
            "model" to deviceInfo.getDeviceName(),
            "locale" to LocaleUtils.getLocale()
        )
        val token = checkNotNull(callFunction("RegisterEhrid", data)["customToken"])
        return FirebaseAuth.getInstance().signInWithCustomToken(token).await().user != null
    }

    /**
     * Returns data from collections covidDataIncrease and covidDataTotal for the given input date (if date is missing, TODAY is used)
     */
    suspend fun getStats(date: String? = null): CovidStatsResponse {
        val data = hashMapOf(
            "idToken" to getIdToken(),
            "date" to date
        )
        val covidStats = callFunction("GetCovidData", data)
        return Gson().fromJson(covidStats.toString(), CovidStatsResponse::class.java)
    }

    /**
     * In the registrations collection, changes the value of lastNotificationStatus attribute to sent and the value of lastNotificationUpdatedAt attribute to CURRENT_TIMESTAMP for a given idToken.
     */
    suspend fun registerNotification() {
        val data = hashMapOf(
            "idToken" to getIdToken()
        )
        callFunction("RegisterNotification", data)
    }

    private suspend fun getIdToken(): String = suspendCoroutine { cont ->
        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.addOnSuccessListener {
                cont.resumeWith(Result.success(it.token!!))
            }?.addOnFailureListener {
                cont.resumeWith(Result.failure(it))
            }
        } else {
            cont.resumeWith(Result.failure(UnauthrorizedException()))
        }
    }

    /**
     * Changes push token
     */
    suspend fun changePushToken(token: String, pushRegistrationToken: String) {
        val data = hashMapOf(
            "idToken" to getIdToken(),
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
        data: Map<String, Any?>? = null,
        onFailure: ((Exception) -> Unit)? = null
    ): Map<String, String> {
        @Suppress("UNCHECKED_CAST")
        return Firebase.functions(FIREBASE_REGION).getHttpsCallable(name).call(data)
            .continueWith { task ->
                (task.result?.data as HashMap<String, String>)
            }
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    if (e is FirebaseFunctionsException) {
                        val code = e.code
                        val details = e.details
                        L.e("Firebase Function $name failed. Reason [$code] + $details")
                    } else {
                        L.e("Firebase Function $name failed. Reason : ${e?.localizedMessage}")
                    }
                    e?.let {
                        onFailure?.invoke(e)
                    }
                }
            }.await()
    }
}