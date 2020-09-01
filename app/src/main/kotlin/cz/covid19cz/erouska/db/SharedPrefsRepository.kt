package cz.covid19cz.erouska.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import cz.covid19cz.erouska.AppConfig

class SharedPrefsRepository(c: Context) {

    companion object {
        const val APP_PAUSED = "preference.app_paused"
        const val LAST_KEY_IMPORT = "preference.last_import"
        const val LAST_KEY_IMPORT_TIME = "preference.last_import_time"
        const val LAST_NOTIFIED_EXPOSURE = "lastNotifiedExposure"
        const val REGISTER_TOKEN = "registerToken"

        const val REPORT_TYPE_WEIGHTS = "reportTypeWeights"
        const val INFECTIOUSNESS_WEIGHTS = "infectiousnessWeights"
        const val ATTENUATION_BUCKET_THRESHOLD_DB = "attenuationBucketThresholdDb"
        const val ATTENUATION_BUCKET_WEIGHTS = "attenuationBucketWeights"
        const val MINIMUM_WINDOW_SCORE = "minimumWindowScore"

        const val REVISION_TOKEN = "revisionToken"

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
        return prefs.getString(LAST_KEY_IMPORT, "") ?: ""
    }

    fun setLastKeyExportFileName(filename: String) {
        prefs.edit().putString(LAST_KEY_IMPORT, filename).apply()
    }

    fun setLastKeyImport(timestamp: Long) {
        prefs.edit().putLong(LAST_KEY_IMPORT_TIME, timestamp).apply()
    }

    fun getLastKeyImport(): Long {
        return prefs.getLong(LAST_KEY_IMPORT_TIME, 0L)
    }

    fun setLastNotifiedExposure(daysSinceEpoch: Int) {
        prefs.edit().putInt(LAST_NOTIFIED_EXPOSURE, daysSinceEpoch).apply()
    }

    fun getLastNotifiedExposure(): Int {
        return prefs.getInt(LAST_NOTIFIED_EXPOSURE, 0)
    }

    fun hasOutdatedKeyData(): Boolean {
        val lastTimestamp = getLastKeyImport()
        return lastTimestamp != 0L && (System.currentTimeMillis() - lastTimestamp) / (1000 * 60 * 60) > AppConfig.keyImportDataOutdatedHours
    }

    fun clearLastKeyExportFileName() {
        prefs.edit().remove(LAST_KEY_IMPORT).apply()
    }

    fun clearLastKeyImportTime() {
        prefs.edit().remove(LAST_KEY_IMPORT_TIME).apply()
    }

    fun isUpdateFromLegacyVersion() = prefs.contains(APP_PAUSED)

    fun markUpdateFromLegacyVersionCompleted() {
        prefs.edit().remove(APP_PAUSED).apply()
    }

    fun isActivated(): Boolean {
        return prefs.contains(REGISTER_TOKEN)
    }

    fun saveRegisterToken(token: String?) {
        prefs.edit().putString(REGISTER_TOKEN, token).apply()
    }

    fun getRegisterToken(): String {
        return checkNotNull(prefs.getString(REGISTER_TOKEN, null))
    }

    fun saveRevisionToken(token: String?) {
        prefs.edit().putString(REVISION_TOKEN, token).apply()
    }

    fun getRevisionToken(): String? {
        return prefs.getString(REVISION_TOKEN, null)
    }

    fun clearCustomConfig() {
        prefs.edit().apply {
            remove(REPORT_TYPE_WEIGHTS)
            remove(ATTENUATION_BUCKET_THRESHOLD_DB)
            remove(ATTENUATION_BUCKET_WEIGHTS)
            remove(MINIMUM_WINDOW_SCORE)
        }.apply()
    }

    fun setReportTypeWeights(value: String) {
        prefs.edit().putString(REPORT_TYPE_WEIGHTS, value).apply()
    }

    fun setInfectiousnessWeights(value: String) {
        prefs.edit().putString(INFECTIOUSNESS_WEIGHTS, value).apply()
    }

    fun setAttenuationBucketThresholdDb(value: String) {
        prefs.edit().putString(ATTENUATION_BUCKET_THRESHOLD_DB, value).apply()
    }

    fun setAttenuationBucketWeights(value: String) {
        prefs.edit().putString(ATTENUATION_BUCKET_WEIGHTS, value).apply()
    }

    fun setMinimumWindowScore(value: String) {
        prefs.edit().putString(MINIMUM_WINDOW_SCORE, value).apply()
    }

    fun getReportTypeWeights(): List<Double>? {
        return prefs.getString(REPORT_TYPE_WEIGHTS, null)?.let {
            it.split(";").mapNotNull { it.toDoubleOrNull() }
        }
    }

    fun getInfectiousnessWeights(): List<Double>? {
        return prefs.getString(INFECTIOUSNESS_WEIGHTS, null)?.let {
            it.split(";").mapNotNull { it.toDoubleOrNull() }
        }
    }

    fun getAttenuationBucketThresholdDb(): List<Int>? {
        return prefs.getString(ATTENUATION_BUCKET_THRESHOLD_DB, null)?.let {
            it.split(";").mapNotNull { it.toIntOrNull() }
        }
    }

    fun getAttenuationBucketWeights(): List<Double>? {
        return prefs.getString(ATTENUATION_BUCKET_WEIGHTS, null)?.let {
            it.split(";").mapNotNull { it.toDoubleOrNull() }
        }
    }

    fun getMinimumWindowScore(): Double? {
        return prefs.getString(MINIMUM_WINDOW_SCORE, null)?.toDoubleOrNull()
    }

    fun getLastStatsUpdate(): Long {
        return prefs.getLong(LAST_STATS_UPDATE, 0)
    }

    fun setLastStatsUpdate(modified: Long) {
        return prefs.edit().putLong(LAST_STATS_UPDATE, modified).apply()
    }

    fun getTestsTotal(): Int {
        return prefs.getInt(TESTS_TOTAL, 0)
    }

    fun setTestsTotal(value: Int) {
        return prefs.edit().putInt(TESTS_TOTAL, value).apply()
    }

    fun getTestsIncrease(): Int {
        return prefs.getInt(TESTS_INCREASE, 0)
    }

    fun setTestsIncrease(value: Int) {
        return prefs.edit().putInt(TESTS_INCREASE, value).apply()
    }

    fun getConfirmedCasesTotal(): Int {
        return prefs.getInt(CONFIRMED_CASES_TOTAL, 0)
    }

    fun setConfirmedCasesTotal(value: Int) {
        return prefs.edit().putInt(CONFIRMED_CASES_TOTAL, value).apply()
    }

    fun getConfirmedCasesIncrease(): Int {
        return prefs.getInt(CONFIRMED_CASES_INCREASE, 0)
    }

    fun setConfirmedCasesIncrease(value: Int) {
        return prefs.edit().putInt(CONFIRMED_CASES_INCREASE, value).apply()
    }

    fun getActiveCasesTotal(): Int {
        return prefs.getInt(ACTIVE_CASES_TOTAL, 0)
    }

    fun setActiveCasesTotal(value: Int) {
        return prefs.edit().putInt(ACTIVE_CASES_TOTAL, value).apply()
    }

    fun getActiveCasesIncrease(): Int {
        return prefs.getInt(ACTIVE_CASES_INCREASE, 0)
    }

    fun setActiveCasesIncrease(value: Int) {
        return prefs.edit().putInt(ACTIVE_CASES_INCREASE, value).apply()
    }

    fun getCuredTotal(): Int {
        return prefs.getInt(CURED_TOTAL, 0)
    }

    fun setCuredTotal(value: Int) {
        return prefs.edit().putInt(CURED_TOTAL, value).apply()
    }

    fun getCuredIncrease(): Int {
        return prefs.getInt(CURED_INCREASE, 0)
    }

    fun setCuredIncrease(value: Int) {
        return prefs.edit().putInt(CURED_INCREASE, value).apply()
    }

    fun getDeceasedTotal(): Int {
        return prefs.getInt(DECEASED_TOTAL, 0)
    }

    fun setDeceasedTotal(value: Int) {
        return prefs.edit().putInt(DECEASED_TOTAL, value).apply()
    }

    fun getDeceasedIncrease(): Int {
        return prefs.getInt(DECEASED_INCREASE, 0)
    }

    fun setDeceasedIncrease(value: Int) {
        return prefs.edit().putInt(DECEASED_INCREASE, value).apply()
    }

    fun getCurrentlyHospitalizedTotal(): Int {
        return prefs.getInt(CURRENTLY_HOSPITALIZED_TOTAL, 0)
    }

    fun setCurrentlyHospitalizedTotal(value: Int) {
        return prefs.edit().putInt(CURRENTLY_HOSPITALIZED_TOTAL, value).apply()
    }

    fun getCurrentlyHospitalizedIncrease(): Int {
        return prefs.getInt(CURRENTLY_HOSPITALIZED_INCREASE, 0)
    }

    fun setCurrentlyHospitalizedIncrease(value: Int) {
        return prefs.edit().putInt(CURRENTLY_HOSPITALIZED_INCREASE, value).apply()
    }
}