package cz.covid19cz.erouska.utils

import android.util.Log
import com.crashlytics.android.Crashlytics
import cz.covid19cz.erouska.BuildConfig

/**
 * Created by stepansonsky on 07/01/16.
 */
object L {

    fun d(text: String) {
        val logStrings = createLogStrings(text)
        if (BuildConfig.DEBUG) {
            Log.d(logStrings[0], logStrings[1])
        }
    }

    fun i(text: String) {
        val logStrings = createLogStrings(text)
        Crashlytics.log(Log.INFO, logStrings[0], logStrings[1])
    }

    fun w(text: String) {
        val logStrings = createLogStrings(text)
        if (BuildConfig.DEBUG) {
            Log.w(logStrings[0], logStrings[1])
        }
        Crashlytics.log(Log.WARN, logStrings[0], logStrings[1])
    }

    fun e(text: String) {
        val logStrings = createLogStrings(text)
       Crashlytics.log(Log.ERROR, logStrings[0], logStrings[1])
    }

    fun e(throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e("L", throwable.message, throwable)
        }
       Crashlytics.logException(throwable)
    }

    private fun createLogStrings(text: String): Array<String> {
        val ste = Thread.currentThread().stackTrace
        val line = "(" + (ste[4].fileName + ":" + ste[4].lineNumber + ")")
        return arrayOf(line, text)
    }
}
