package cz.covid19cz.erouska.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

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
        const val LAST_STATS_UPDATE = "lastStatsUpdate"


        const val TESTS_TOTAL = "testsTotal"
        const val TESTS_INCREASE = "testsIncrease"

        const val CONFIRMED_CASES_TOTAL = "confirmedCasesTotal"
        const val CONFIRMED_CASES_INCREASE = "confirmedCasesIncrease"

        const val ACTIVE_CASES_TOTAL = "activeCasesTotal"
        const val ACTIVE_CASES_INCREASE = "activeCasesIncrease"

        const val CURED_TOTAL = "curedTotal"
        const val CURED_INCREASE = "curedIncrease"

        const val DECEASED_TOTAL = "deceasedTotal"
        const val DECEASED_INCREASE = "deceasedIncrease"

        const val CURRENTLY_HOSPITALIZED_TOTAL = "currentlyHospitalizedTotal"
        const val CURRENTLY_HOSPITALIZED_INCREASE = "currentlyHospitalizedIncrease"
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

    fun getLastStatsUpdate() : Long{
        return prefs.getLong(LAST_STATS_UPDATE, 0)
    }

    fun setLastStatsUpdate(lastUpdate: Long){
        return prefs.edit().putLong(LAST_STATS_UPDATE, lastUpdate).apply()
    }

    fun getTestsTotal() : Int{
        return prefs.getInt(TESTS_TOTAL, 0)
    }
    fun setTestsTotal(value: Int){
        return prefs.edit().putInt(TESTS_TOTAL, value).apply()
    }
    fun getTestsIncrease() : Int{
        return prefs.getInt(TESTS_INCREASE, 0)
    }
    fun setTestsIncrease(value: Int){
        return prefs.edit().putInt(TESTS_INCREASE, value).apply()
    }

    fun getConfirmedCasesTotal() : Int{
        return prefs.getInt(CONFIRMED_CASES_TOTAL, 0)
    }
    fun setConfirmedCasesTotal(value: Int){
        return prefs.edit().putInt(CONFIRMED_CASES_TOTAL, value).apply()
    }
    fun getConfirmedCasesIncrease() : Int{
        return prefs.getInt(CONFIRMED_CASES_INCREASE, 0)
    }
    fun setConfirmedCasesIncrease(value: Int){
        return prefs.edit().putInt(CONFIRMED_CASES_INCREASE, value).apply()
    }

    fun getActiveCasesTotal() : Int{
        return prefs.getInt(ACTIVE_CASES_TOTAL, 0)
    }
    fun setActiveCasesTotal(value: Int){
        return prefs.edit().putInt(ACTIVE_CASES_TOTAL, value).apply()
    }
    fun getActiveCasesIncrease() : Int{
        return prefs.getInt(ACTIVE_CASES_INCREASE, 0)
    }
    fun setActiveCasesIncrease(value: Int){
        return prefs.edit().putInt(ACTIVE_CASES_INCREASE, value).apply()
    }

    fun getCuredTotal() : Int{
        return prefs.getInt(CURED_TOTAL, 0)
    }
    fun setCuredTotal(value: Int){
        return prefs.edit().putInt(CURED_TOTAL, value).apply()
    }
    fun getCuredIncrease() : Int{
        return prefs.getInt(CURED_INCREASE, 0)
    }
    fun setCuredIncrease(value: Int){
        return prefs.edit().putInt(CURED_INCREASE, value).apply()
    }

    fun getDeceasedTotal() : Int{
        return prefs.getInt(DECEASED_TOTAL, 0)
    }
    fun setDeceasedTotal(value: Int){
        return prefs.edit().putInt(DECEASED_TOTAL, value).apply()
    }
    fun getDeceasedIncrease() : Int{
        return prefs.getInt(DECEASED_INCREASE, 0)
    }
    fun setDeceasedIncrease(value: Int){
        return prefs.edit().putInt(DECEASED_INCREASE, value).apply()
    }

    fun getCurrentlyHospitalizedTotal() : Int{
        return prefs.getInt(CURRENTLY_HOSPITALIZED_TOTAL, 0)
    }
    fun setCurrentlyHospitalizedTotal(value: Int){
        return prefs.edit().putInt(CURRENTLY_HOSPITALIZED_TOTAL, value).apply()
    }
    fun getCurrentlyHospitalizedIncrease() : Int{
        return prefs.getInt(CURRENTLY_HOSPITALIZED_INCREASE, 0)
    }
    fun setCurrentlyHospitalizedIncrease(value: Int){
        return prefs.edit().putInt(CURRENTLY_HOSPITALIZED_INCREASE, value).apply()
    }
}