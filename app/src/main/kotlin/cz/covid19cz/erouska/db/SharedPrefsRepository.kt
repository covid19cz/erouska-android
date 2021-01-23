package cz.covid19cz.erouska.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsRepository @Inject constructor(@ApplicationContext c: Context) {

    companion object {
        const val APP_PAUSED = "preference.app_paused"
        const val LAST_KEY_IMPORT = "preference.last_import"
        const val LAST_KEY_IMPORT_TIME = "preference.last_import_time"
        const val LAST_NOTIFIED_EXPOSURE = "lastNotifiedExposure"
        const val LAST_IN_APP_NOTIFIED_EXPOSURE = "lastInAppNotifiedExposure"
        const val LAST_SHOWN_EXPOSURE_INFO = "lastShownExposureInfo"
        const val EXPOSURE_NOTIFICATIONS_ENABLED = "exposureNotificationsEnabled"
        const val LAST_SET_DIAGNOSIS_KEYS_DATA_MAPPING = "lastSetDiagnosisKeysDataMapping"
        const val APP_OPEN_TIMESTAMP = "lastTimeAppOpened"

        const val REPORT_TYPE_WEIGHTS = "reportTypeWeights"
        const val INFECTIOUSNESS_WEIGHTS = "infectiousnessWeights"
        const val ATTENUATION_BUCKET_THRESHOLD_DB = "attenuationBucketThresholdDb"
        const val ATTENUATION_BUCKET_WEIGHTS = "attenuationBucketWeights"
        const val MINIMUM_WINDOW_SCORE = "minimumWindowScore"

        const val LAST_STATS_UPDATE = "lastStatsUpdate"
        const val LAST_METRICS_UPDATE = "lastMetricsUpdate"

        // stats
        const val TESTS_TOTAL = "testsTotal"
        const val TESTS_INCREASE = "testsIncrease"
        const val TESTS_INCREASE_DATE = "testsIncreaseDate"

        const val ANTIGEN_TESTS_TOTAL = "antigenTestsTotal"
        const val ANTIGEN_TESTS_INCREASE = "antigenTestsIncrease"
        const val ANTIGEN_TESTS_INCREASE_DATE = "antigenTestsIncreaseDate"

        const val CONFIRMED_CASES_TOTAL = "confirmedCasesTotal"
        const val CONFIRMED_CASES_INCREASE = "confirmedCasesIncrease"
        const val CONFIRMED_CASES_INCREASE_DATE = "confirmedCasesIncreaseDate"

        const val ACTIVE_CASES_TOTAL = "activeCasesTotal"
        const val CURED_TOTAL = "curedTotal"
        const val DECEASED_TOTAL = "deceasedTotal"
        const val CURRENTLY_HOSPITALIZED_TOTAL = "currentlyHospitalizedTotal"

        // metrics
        const val ACTIVATIONS_TOTAL = "activationsTotal"
        const val ACTIVATIONS_YESTERDAY = "activationsIncrease"
        const val KEY_PUBLISHERS_TOTAL = "keyPublishersTotal"
        const val KEY_PUBLISHERS_YESTERDAY = "keyPublishersYesterday"
        const val NOTIFICATIONS_TOTAL = "notificationsTotal"
        const val NOTIFICATIONS_YESTERDAY = "notificationsTotal"
        const val CURRENTLY_HOSPITALIZED_INCREASE = "currentlyHospitalizedIncrease"

        const val LEGACY_EXPOSURES_IMPORTED = "legacyExposuresImported"
        const val PUSH_TOKEN_REGISTERED = "pushTokenRegistered"
        const val PUSH_TOPIC_REGISTERED = "pushTopicRegistered"
    }

    private val prefs: SharedPreferences = c.getSharedPreferences("prefs", MODE_PRIVATE)
    val lastKeyImportLive = SafeMutableLiveData(getLastKeyImport())

    fun lastKeyExportFileName(): String {
        return prefs.getString(LAST_KEY_IMPORT, "") ?: ""
    }

    fun setLastKeyExportFileName(filename: String) {
        prefs.edit().putString(LAST_KEY_IMPORT, filename).apply()
    }

    fun setLastKeyImport() {
        val timestamp = System.currentTimeMillis()
        prefs.edit().putLong(LAST_KEY_IMPORT_TIME, timestamp).apply()
        lastKeyImportLive.postValue(timestamp)
    }

    fun getLastKeyImport(): Long {
        return prefs.getLong(LAST_KEY_IMPORT_TIME, 0L)
    }

    fun setLastSetDiagnosisKeysDataMapping() {
        prefs.edit().putLong(LAST_SET_DIAGNOSIS_KEYS_DATA_MAPPING, System.currentTimeMillis())
            .apply()
    }

    fun getLastSetDiagnosisKeysDataMapping(): Long {
        return prefs.getLong(LAST_SET_DIAGNOSIS_KEYS_DATA_MAPPING, 0L)
    }

    fun getLastNotifiedExposure(): Int {
        return prefs.getInt(LAST_NOTIFIED_EXPOSURE, -1)
    }

    fun getLastInAppNotifiedExposure(): Int {
        return prefs.getInt(LAST_IN_APP_NOTIFIED_EXPOSURE, 0)
    }

    fun setLastShownExposureInfo(daysSinceEpoch: Int) {
        prefs.edit().putInt(LAST_SHOWN_EXPOSURE_INFO, daysSinceEpoch).apply()
    }

    fun getLastShownExposureInfo(): Int {
        return prefs.getInt(LAST_SHOWN_EXPOSURE_INFO, 0)
    }

    fun cleanLegacyExposurePrefs() {
        prefs.edit().remove(LAST_NOTIFIED_EXPOSURE).remove(LAST_IN_APP_NOTIFIED_EXPOSURE).apply()
    }

    fun isLegacyExposuresImported(): Boolean {
        return prefs.getBoolean(LEGACY_EXPOSURES_IMPORTED, false)
    }

    fun setLegacyExposuresImported() {
        prefs.edit().putBoolean(LEGACY_EXPOSURES_IMPORTED, true).apply()
    }

    fun isPushTokenRegistered(): Boolean {
        return prefs.getBoolean(PUSH_TOKEN_REGISTERED, false)
    }

    fun setPushTokenRegistered() {
        prefs.edit().putBoolean(PUSH_TOKEN_REGISTERED, true).apply()
    }

    fun isPushTopicRegistered(): Boolean {
        return prefs.getBoolean(PUSH_TOPIC_REGISTERED, false)
    }

    fun setPushTopicRegistered() {
        prefs.edit().putBoolean(PUSH_TOPIC_REGISTERED, true).apply()
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

    fun isExposureNotificationsEnabled(): Boolean {
        return prefs.getBoolean(EXPOSURE_NOTIFICATIONS_ENABLED, false)
    }

    fun setExposureNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(EXPOSURE_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun setAppVisitedTimestamp() {
        prefs.edit().putLong(APP_OPEN_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    fun getLastTimeAppOpened(): Long {
        return prefs.getLong(APP_OPEN_TIMESTAMP, 0L)
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

    fun getLastMetricsUpdate(): Long {
        return prefs.getLong(LAST_METRICS_UPDATE, 0)
    }

    fun setLastMetricsUpdate(modified: Long) {
        return prefs.edit().putLong(LAST_METRICS_UPDATE, modified).apply()
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

    fun getTestsIncreaseDate(): Long {
        return prefs.getLong(TESTS_INCREASE_DATE, 0)
    }

    fun setTestsIncreaseDate(value: Long) {
        return prefs.edit().putLong(TESTS_INCREASE_DATE, value).apply()
    }

    fun getAntigenTestsTotal(): Int {
        return prefs.getInt(ANTIGEN_TESTS_TOTAL, 0)
    }

    fun setAntigenTestsTotal(value: Int) {
        return prefs.edit().putInt(ANTIGEN_TESTS_TOTAL, value).apply()
    }

    fun getAntigenTestsIncrease(): Int {
        return prefs.getInt(ANTIGEN_TESTS_INCREASE, 0)
    }

    fun setAntigenTestsIncrease(value: Int) {
        return prefs.edit().putInt(ANTIGEN_TESTS_INCREASE, value).apply()
    }

    fun getAntigenTestsIncreaseDate(): Long {
        return prefs.getLong(ANTIGEN_TESTS_INCREASE_DATE, 0)
    }

    fun setAntigenTestsIncreaseDate(value: Long) {
        return prefs.edit().putLong(ANTIGEN_TESTS_INCREASE_DATE, value).apply()
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

    fun getConfirmedCasesIncreaseDate(): Long {
        return prefs.getLong(CONFIRMED_CASES_INCREASE_DATE, 0)
    }

    fun setConfirmedCasesIncreaseDate(value: Long) {
        return prefs.edit().putLong(CONFIRMED_CASES_INCREASE_DATE, value).apply()
    }

    fun getActiveCasesTotal(): Int {
        return prefs.getInt(ACTIVE_CASES_TOTAL, 0)
    }

    fun setActiveCasesTotal(value: Int) {
        return prefs.edit().putInt(ACTIVE_CASES_TOTAL, value).apply()
    }

    fun getCuredTotal(): Int {
        return prefs.getInt(CURED_TOTAL, 0)
    }

    fun setCuredTotal(value: Int) {
        return prefs.edit().putInt(CURED_TOTAL, value).apply()
    }

    fun getDeceasedTotal(): Int {
        return prefs.getInt(DECEASED_TOTAL, 0)
    }

    fun setDeceasedTotal(value: Int) {
        return prefs.edit().putInt(DECEASED_TOTAL, value).apply()
    }

    fun getCurrentlyHospitalizedTotal(): Int {
        return prefs.getInt(CURRENTLY_HOSPITALIZED_TOTAL, 0)
    }

    fun setCurrentlyHospitalizedTotal(value: Int) {
        return prefs.edit().putInt(CURRENTLY_HOSPITALIZED_TOTAL, value).apply()
    }

    fun getActivationsTotal(): Int {
        return prefs.getInt(ACTIVATIONS_TOTAL, 0)
    }

    fun setActivationsTotal(value: Int) {
        return prefs.edit().putInt(ACTIVATIONS_TOTAL, value).apply()
    }

    fun getActivationsYesterday(): Int {
        return prefs.getInt(ACTIVATIONS_YESTERDAY, 0)
    }

    fun setActivationsYesterday(value: Int) {
        return prefs.edit().putInt(ACTIVATIONS_YESTERDAY, value).apply()
    }

    fun getKeyPublishersTotal(): Int {
        return prefs.getInt(KEY_PUBLISHERS_TOTAL, 0)
    }

    fun setKeyPublishersTotal(value: Int) {
        return prefs.edit().putInt(KEY_PUBLISHERS_TOTAL, value).apply()
    }

    fun getKeyPublishersYesterday(): Int {
        return prefs.getInt(KEY_PUBLISHERS_YESTERDAY, 0)
    }

    fun setKeyPublishersYesterday(value: Int) {
        return prefs.edit().putInt(KEY_PUBLISHERS_YESTERDAY, value).apply()
    }

    fun getNotificationsTotal(): Int {
        return prefs.getInt(NOTIFICATIONS_TOTAL, 0)
    }

    fun setNotificationsTotal(value: Int) {
        return prefs.edit().putInt(NOTIFICATIONS_TOTAL, value).apply()
    }

    fun getNotificationsYesterday(): Int {
        return prefs.getInt(NOTIFICATIONS_YESTERDAY, 0)
    }

    fun setNotificationsYesterday(value: Int) {
        return prefs.edit().putInt(NOTIFICATIONS_YESTERDAY, value).apply()
    }
}