package cz.covid19cz.erouska.ui.mydata

import android.text.format.DateUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import arch.utils.safeLet
import com.google.android.gms.common.api.ApiException
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MyDataVM @Inject constructor(
    private val firebaseFunctionsRepository: FirebaseFunctionsRepository,
    val prefs: SharedPrefsRepository
) : BaseVM() {

    companion object {
        const val LAST_UPDATE_UI_FORMAT = "d. M. yyyy" // date format used in UI
        const val LAST_UPDATE_API_FORMAT = "yyyyMMdd" // date format returned from API
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (AppConfig.updateNewsOnRequest || (!AppConfig.updateNewsOnRequest && !DateUtils.isToday(prefs.getLastStatsUpdate()))) {
            getStats()
        }

        if (AppConfig.updateNewsOnRequest || (!AppConfig.updateNewsOnRequest && !DateUtils.isToday(prefs.getLastMetricsUpdate()))) {
            getMetrics()
        }

    }

    fun onRefresh() {
        getMetrics()
        getStats()
    }

    val isLoading = SafeMutableLiveData(false)

    // vaccinations
    val vaccinationsTotal = SafeMutableLiveData(prefs.getVaccinationsTotal())
    val vaccinationsIncrease = SafeMutableLiveData(prefs.getVaccinationsIncrease())
    val vaccinationsIncreaseDate = if (prefs.getVaccinationsIncreaseDate() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(prefs.getVaccinationsIncreaseDate()))
        )
    }


    val firstDoseTotal = SafeMutableLiveData(prefs.getFirstDoseTotal())
    val firstDoseIncrease = SafeMutableLiveData(prefs.getFirstDoseIncrease())

    val secondDoseTotal = SafeMutableLiveData(prefs.getSecondDoseTotal())
    val secondDoseIncrease = SafeMutableLiveData(prefs.getSecondDoseIncrease())

    val dailyDosesDate = if (prefs.getDailyDosesDate() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(prefs.getDailyDosesDate()))
        )
    }

    // stats
    val testsTotal = SafeMutableLiveData(prefs.getTestsTotal())
    val testsIncrease = SafeMutableLiveData(prefs.getTestsIncrease())
    val testsIncreaseDate = if (prefs.getTestsIncreaseDate() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(prefs.getTestsIncreaseDate()))
        )
    }

    val antigenTestsTotal = SafeMutableLiveData(prefs.getAntigenTestsTotal())
    val antigenTestsIncrease = SafeMutableLiveData(prefs.getAntigenTestsIncrease())
    val antigenTestsIncreaseDate = if (prefs.getAntigenTestsIncreaseDate() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(prefs.getAntigenTestsIncreaseDate()))
        )
    }

    val confirmedCasesTotal = SafeMutableLiveData(prefs.getConfirmedCasesTotal())
    val confirmedCasesIncrease = SafeMutableLiveData(prefs.getConfirmedCasesIncrease())
    val confirmedCasesIncreaseDate= if (prefs.getConfirmedCasesIncreaseDate() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(prefs.getConfirmedCasesIncreaseDate()))
        )
    }

    val activeCasesTotal = SafeMutableLiveData(prefs.getActiveCasesTotal())
    val curedTotal = SafeMutableLiveData(prefs.getCuredTotal())
    val deceasedTotal = SafeMutableLiveData(prefs.getDeceasedTotal())
    val currentlyHospitalizedTotal = SafeMutableLiveData(prefs.getCurrentlyHospitalizedTotal())

    // metrics
    val activationsTotal = SafeMutableLiveData(prefs.getActivationsTotal())
    val activationsYesterday = SafeMutableLiveData(prefs.getActivationsYesterday())
    val keyPublishersTotal = SafeMutableLiveData(prefs.getKeyPublishersTotal())
    val keyPublishersYesterday = SafeMutableLiveData(prefs.getKeyPublishersYesterday())
    val notificationsTotal = SafeMutableLiveData(prefs.getNotificationsTotal())
    val notificationsYesterday = SafeMutableLiveData(prefs.getNotificationsYesterday())

    var lastMetricsIncreaseDate = if (prefs.getLastMetricsUpdate() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(prefs.getLastMetricsUpdate() - TimeUnit.DAYS.toMillis(1))) // increase day = day before day of last update
        )
    }

    fun getMeasuresUrl() = AppConfig.currentMeasuresUrl

    private fun getStats(date: String? = null) {
        isLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                return@runCatching firebaseFunctionsRepository.getStats(date)
            }.onSuccess { response ->
                L.d(response.toString())
                isLoading.value = false

                safeLet(response.testsTotal,
                    response.testsIncrease,
                    response.testsIncreaseDate) { total, increase, increaseDate ->
                    testsTotal.value = total
                    testsIncrease.value = increase

                    prefs.setTestsTotal(total)
                    prefs.setTestsIncrease(increase)

                    val lastUpdateDate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(increaseDate)

                    lastUpdateDate?.time?.let { lastUpdateMillis ->
                        prefs.setTestsIncreaseDate(lastUpdateMillis)

                        testsIncreaseDate.value = SimpleDateFormat(
                            LAST_UPDATE_UI_FORMAT,
                            Locale.getDefault()
                        ).format(
                            Date(lastUpdateMillis)
                        )
                    }
                }
               safeLet(response.antigenTestsTotal,
                    response.antigenTestsIncrease,
                    response.antigenTestsIncreaseDate) { total, increase, increaseDate ->
                    antigenTestsTotal.value = total
                    antigenTestsIncrease.value = increase

                    prefs.setAntigenTestsTotal(total)
                    prefs.setAntigenTestsIncrease(increase)

                    val lastUpdateDate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(increaseDate)

                    lastUpdateDate?.time?.let { lastUpdateMillis ->
                        prefs.setAntigenTestsIncreaseDate(lastUpdateMillis)

                        antigenTestsIncreaseDate.value = SimpleDateFormat(
                            LAST_UPDATE_UI_FORMAT,
                            Locale.getDefault()
                        ).format(
                            Date(lastUpdateMillis)
                        )
                    }
                }
               safeLet(response.vaccinationsTotal,
                    response.vaccinationsIncrease,
                    response.vaccinationsIncreaseDate) { total, increase, increaseDate ->
                    vaccinationsTotal.value = total
                    vaccinationsIncrease.value = increase

                    prefs.setVaccinationsTotal(total)
                    prefs.setVaccinationsIncrease(increase)

                    val lastUpdateDate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(increaseDate)

                    lastUpdateDate?.time?.let { lastUpdateMillis ->
                        prefs.setVaccinationsIncreaseDate(lastUpdateMillis)

                        vaccinationsIncreaseDate.value = SimpleDateFormat(
                            LAST_UPDATE_UI_FORMAT,
                            Locale.getDefault()
                        ).format(
                            Date(lastUpdateMillis)
                        )
                    }
               }

               safeLet(response.vaccinationsTotalFirstDose,
                    response.vaccinationsDailyFirstDose) { total, increase ->
                   firstDoseTotal.value = total
                   firstDoseIncrease.value = increase

                    prefs.setFirstDoseTotal(total)
                    prefs.setFirstDoseIncrease(increase)
                }
               safeLet(response.vaccinationsTotalSecondDose,
                    response.vaccinationsDailySecondDose) { total, increase ->
                   secondDoseTotal.value = total
                   secondDoseIncrease.value = increase

                    prefs.setSecondDoseTotal(total)
                    prefs.setSecondDoseIncrease(increase)
                }

                response.vaccinationsDailyDosesDate?.let { increaseDate ->
                    val lastUpdateDate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(increaseDate)

                    lastUpdateDate?.time?.let { lastUpdateMillis ->
                        prefs.setDailyDosesDate(lastUpdateMillis)

                        dailyDosesDate.value = SimpleDateFormat(
                            LAST_UPDATE_UI_FORMAT,
                            Locale.getDefault()
                        ).format(
                            Date(lastUpdateMillis)
                        )
                    }
                }

                safeLet(
                    response.confirmedCasesTotal,
                    response.confirmedCasesIncrease,
                    response.confirmedCasesIncreaseDate,
                ) { total, increase, increaseDate ->
                    confirmedCasesTotal.value = total
                    confirmedCasesIncrease.value = increase

                    prefs.setConfirmedCasesTotal(total)
                    prefs.setConfirmedCasesIncrease(increase)

                    val lastUpdateDate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(increaseDate)

                    lastUpdateDate?.time?.let { lastUpdateMillis ->
                        prefs.setConfirmedCasesIncreaseDate(lastUpdateMillis)

                        confirmedCasesIncreaseDate.value = SimpleDateFormat(
                            LAST_UPDATE_UI_FORMAT,
                            Locale.getDefault()
                        ).format(
                            Date(lastUpdateMillis)
                        )
                    }
                }
                response.activeCasesTotal?.let { total ->
                    activeCasesTotal.value = total
                    prefs.setActiveCasesTotal(total)
                }
                response.curedTotal?.let { total ->
                    curedTotal.value = total
                    prefs.setCuredTotal(total)
                }
                response.deceasedTotal?.let { total ->
                    deceasedTotal.value = total
                    prefs.setDeceasedTotal(total)
                }
                response.currentlyHospitalizedTotal?.let { total ->
                    currentlyHospitalizedTotal.value = total
                    prefs.setCurrentlyHospitalizedTotal(total)
                }

                response.date?.let {
                    val lastStatsUpdate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(response.date)

                    lastStatsUpdate?.time?.let { lastUpdateMillis ->
                        prefs.setLastStatsUpdate(lastUpdateMillis)
                    }
                }
            }.onFailure {
                isLoading.value = false
                if (it is ApiException) {
                    L.e(it.status.toString() + " " + it.message)
                } else {
                    L.e(it)
                }
            }
        }
    }

    private fun getMetrics() {
        isLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                return@runCatching firebaseFunctionsRepository.getDownloadMetrics()
            }.onSuccess { response ->
                L.d(response.toString())
                isLoading.value = false

                safeLet(
                    response.activationsTotal,
                    response.activationsYesterday
                ) { total, yesterday ->
                    activationsTotal.value = total
                    activationsYesterday.value = yesterday

                    prefs.setActivationsTotal(total)
                    prefs.setActivationsYesterday(yesterday)
                }
                safeLet(
                    response.keyPublishersTotal,
                    response.keyPublishersYesterday
                ) { total, yesterday ->
                    keyPublishersTotal.value = total
                    keyPublishersYesterday.value = yesterday

                    prefs.setKeyPublishersTotal(total)
                    prefs.setKeyPublishersYesterday(yesterday)
                }
                safeLet(
                    response.notificationsTotal,
                    response.notificationsYesterday
                ) { total, yesterday ->
                    notificationsTotal.value = total
                    notificationsYesterday.value = yesterday

                    prefs.setNotificationsTotal(total)
                    prefs.setNotificationsYesterday(yesterday)
                }

                response.date?.let {
                    val lastMetricsUpdate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(response.date)

                    lastMetricsUpdate?.time?.let { lastUpdateMillis ->
                        prefs.setLastMetricsUpdate(lastUpdateMillis)
                        val lastMetricsIncreaseMillis = lastUpdateMillis - TimeUnit.DAYS.toMillis(1) // increase day = day before day of last update

                        lastMetricsIncreaseDate.value = SimpleDateFormat(
                            LAST_UPDATE_UI_FORMAT,
                            Locale.getDefault()
                        ).format(
                            Date(lastMetricsIncreaseMillis)
                        )
                    }
                }
            }.onFailure {
                isLoading.value = false
                if (it is ApiException) {
                    L.e(it.status.toString() + " " + it.message)
                } else {
                    L.e(it)
                }
            }
        }
    }
}