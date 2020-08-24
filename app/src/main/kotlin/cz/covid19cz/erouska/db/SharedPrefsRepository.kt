package cz.covid19cz.erouska.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import cz.covid19cz.erouska.AppConfig

class SharedPrefsRepository(c: Context) {

    companion object {
        const val APP_PAUSED = "preference.app_paused"
        const val LAST_KEY_EXPORT = "preference.last_export"
        const val LAST_KEY_EXPORT_TIME = "preference.last_export_time"
        const val EHRID = "preference.ehrid"

        const val REPORT_TYPE_WEIGHTS = "reportTypeWeights"
        const val INFECTIOUSNESS_WEIGHTS = "infectiousnessWeights"
        const val ATTENUATION_BUCKET_THRESHOLD_DB= "attenuationBucketThresholdDb"
        const val ATTENUATION_BUCKET_WEIGHTS = "attenuationBucketWeights"
        const val MINIMUM_WINDOW_SCORE = "minimumWindowScore"
    }

    private val prefs: SharedPreferences = c.getSharedPreferences("prefs", MODE_PRIVATE)

    fun lastKeyExportFileName(): String {
        return prefs.getString(LAST_KEY_EXPORT, "") ?: ""
    }

    fun setLastKeyExportFileName(filename: String) {
        prefs.edit().putString(LAST_KEY_EXPORT, filename).apply()
    }

    fun addLastKeyExportTime(time: String) {
        val timestamps = prefs.getString(LAST_KEY_EXPORT_TIME, "") ?: ""
        val timestampsList = timestamps.split(",").toMutableList()
        timestampsList.add(time)
        prefs.edit().putString(LAST_KEY_EXPORT_TIME, timestampsList.joinToString(",").trim(',')).apply()
    }

    fun keyExportTimeHistory(): List<String> {
        val timestamps = prefs.getString(LAST_KEY_EXPORT_TIME, "") ?: ""
        return timestamps.split(",")
    }

    fun lastKeyExportTime(): String {
        val history = prefs.getString(LAST_KEY_EXPORT_TIME, "") ?: ""
        val historyList = history.split(",")
        return historyList.max() ?: ""
    }

    fun clearLastKeyExportFileName() {
        prefs.edit().remove(LAST_KEY_EXPORT).apply()
    }

    fun clearLastKeyExportTime() {
        prefs.edit().remove(LAST_KEY_EXPORT_TIME).apply()
    }

    fun isUpdateFromLegacyVersion() = prefs.contains(APP_PAUSED)

    fun markUpdateFromLegacyVersionCompleted() {
        prefs.edit().remove(APP_PAUSED).apply()
    }

    fun saveEhrid(ehrid: String) {
        prefs.edit().putString(EHRID, ehrid).apply()
    }

    fun isActivated(): Boolean {
        return prefs.contains(EHRID)
    }

    fun getEhrid(): String {
        return checkNotNull(prefs.getString(EHRID, null))
    }

    fun clearCustomConfig(){
        prefs.edit().apply {
            remove(REPORT_TYPE_WEIGHTS)
            remove(ATTENUATION_BUCKET_THRESHOLD_DB)
            remove(ATTENUATION_BUCKET_WEIGHTS)
            remove(MINIMUM_WINDOW_SCORE)
        }.apply()
    }

    fun setReportTypeWeights(value : String){
        prefs.edit().putString(REPORT_TYPE_WEIGHTS, value).apply()
    }

    fun setInfectiousnessWeights(value : String){
        prefs.edit().putString(INFECTIOUSNESS_WEIGHTS, value).apply()
    }

    fun setAttenuationBucketThresholdDb(value : String){
        prefs.edit().putString(ATTENUATION_BUCKET_THRESHOLD_DB, value).apply()
    }

    fun setAttenuationBucketWeights(value : String){
        prefs.edit().putString(ATTENUATION_BUCKET_WEIGHTS, value).apply()
    }

    fun setMinimumWindowScore(value : String){
        prefs.edit().putString(MINIMUM_WINDOW_SCORE, value).apply()
    }

    fun getReportTypeWeights() : List<Double>?{
        return prefs.getString(REPORT_TYPE_WEIGHTS, null)?.let {
            it.split(";").mapNotNull { it.toDoubleOrNull() }
        }
    }

    fun getInfectiousnessWeights() : List<Double>?{
        return prefs.getString(INFECTIOUSNESS_WEIGHTS, null)?.let {
            it.split(";").mapNotNull { it.toDoubleOrNull() }
        }
    }

    fun getAttenuationBucketThresholdDb() : List<Int>?{
        return prefs.getString(ATTENUATION_BUCKET_THRESHOLD_DB, null)?.let {
            it.split(";").mapNotNull { it.toIntOrNull() }
        }
    }

    fun getAttenuationBucketWeights() : List<Double>?{
        return prefs.getString(ATTENUATION_BUCKET_WEIGHTS, null)?.let {
            it.split(";").mapNotNull { it.toDoubleOrNull() }
        }
    }

    fun getMinimumWindowScore() : Double?{
        return prefs.getString(MINIMUM_WINDOW_SCORE, null)?.toDoubleOrNull()
    }
}