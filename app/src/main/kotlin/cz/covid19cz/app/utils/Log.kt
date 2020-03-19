package cz.covid19cz.app.utils

import android.util.Log
import cz.covid19cz.app.BuildConfig

/**
 * Created by stepansonsky on 07/01/16.
 */
object Log {

    fun v(text: String) {
        if (BuildConfig.DEBUG) {
            val logStrings = createLogStrings(text)
            Log.v(logStrings[0], logStrings[1])
        }
    }

    fun d(text: String) {
        if (BuildConfig.DEBUG) {
            val logStrings = createLogStrings(text)
            Log.d(logStrings[0], logStrings[1])
        }
    }

    fun w(text: String) {
        if (BuildConfig.DEBUG) {
            val logStrings = createLogStrings(text)
            Log.w(logStrings[0], logStrings[1])
        }
    }

    fun i(text: String) {
        if (BuildConfig.DEBUG) {
            val logStrings = createLogStrings(text)
            Log.i(logStrings[0], logStrings[1])
        }
    }

    fun e(text: String) {
        if (BuildConfig.DEBUG) {
            val logStrings = createLogStrings(text)
            Log.e(logStrings[0], logStrings[1])
        }
    }

    fun e(throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e("Log", throwable.message, throwable)
        }
    }

    private fun createLogStrings(text: String): Array<String> {
        val ste = Thread.currentThread().stackTrace

        val line = "(" + (ste[4].fileName + ":" + ste[4].lineNumber + ")")
        val method = ste[4].methodName + ": " + text
        return arrayOf(line, method)
    }
}
