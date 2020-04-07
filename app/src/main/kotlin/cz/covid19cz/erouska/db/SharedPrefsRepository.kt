package cz.covid19cz.erouska.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SharedPrefsRepository(c : Context) {

    companion object{
        const val DEVICE_BUID = "DEVICE_BUID"
        const val DEVICE_TUIDS = "DEVICE_TUIDS"
        const val CURRENT_TUID = "CURRENT_TUID"
        const val APP_PAUSED = "preference.app_paused"
        const val LAST_UPLOAD_TIMESTAMP = "preference.last_upload_timestamp"
        const val LAST_DB_CLEANUP_TIMESTAMP = "preference.last_db_cleanup_timestamp"
    }

    val prefs : SharedPreferences = c.getSharedPreferences("prefs", MODE_PRIVATE)

    fun putDeviceBuid(buid : String){
        prefs.edit().putString(DEVICE_BUID, buid).apply()
    }

    fun putDeviceTuids(tuids : List<String>){
        prefs.edit().putStringSet(DEVICE_TUIDS, tuids.toSet()).apply()
    }

    fun removeDeviceBuid(){
        prefs.edit().remove(DEVICE_BUID).apply()
    }

    fun getDeviceBuid() : String?{
        return prefs.getString(DEVICE_BUID, null)
    }

    /**
     * Returns random element from the list of tuids.
     * It has an optional parameter which can be used to make sure that no two tuids will be used
     * twice in a row.
     */
    fun getRandomTuid(lastTuid: String? = null) : String?{
        val stringSet = prefs.getStringSet(DEVICE_TUIDS, emptySet())
        return stringSet?.let {
            if (it.size > 1) { // make sure we don't loop in here forever
                it.random()?.run {
                    if (this == lastTuid) getRandomTuid(lastTuid) else this
                }
            } else {
                it.firstOrNull()
            }
        }
    }

    fun getCurrentTuid() = prefs.getString(CURRENT_TUID, null)

    fun setCurrentTuid(tuid: String) = prefs.edit().putString(CURRENT_TUID, tuid).apply()

    fun setAppPaused(appPaused: Boolean) {
        prefs.edit().putBoolean(APP_PAUSED, appPaused).apply()
    }

    fun saveLastUploadTimestamp(timestamp: Long) {
        prefs.edit().putLong(LAST_UPLOAD_TIMESTAMP, timestamp).apply()
    }

    fun getLastUploadTimestamp(): Long {
        return prefs.getLong(LAST_UPLOAD_TIMESTAMP, -1)
    }

    fun saveLastDbCleanupTimestamp(timestamp: Long) {
        prefs.edit().putLong(LAST_DB_CLEANUP_TIMESTAMP, timestamp).apply()
    }

    fun getLastDbCleanupTimestamp(): Long {
        return prefs.getLong(LAST_DB_CLEANUP_TIMESTAMP, 0)
    }

    fun getAppPaused() = prefs.getBoolean(APP_PAUSED, false)

    fun clear(){
        prefs.edit().clear().apply()
    }
}