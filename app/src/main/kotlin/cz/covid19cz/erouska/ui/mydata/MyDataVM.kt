package cz.covid19cz.erouska.ui.mydata

import android.text.format.DateUtils
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.android.gms.common.api.ApiException
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.FirebaseFunctionsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyDataVM @ViewModelInject constructor(
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

    // stats
    val testsTotal = setTotal { prefs.getTestsTotal() }
    val testsIncrease = setIncrease { prefs.getTestsIncrease() }
    val testsIncreaseDate = setIncreaseDate { prefs.getTestsIncreaseDate() }

    val antigenTestsTotal = setTotal { prefs.getAntigenTestsTotal() }
    val antigenTestsIncrease = setIncrease { prefs.getAntigenTestsIncrease() }
    val antigenTestsIncreaseDate = setIncreaseDate { prefs.getTestsIncreaseDate() }

    val vaccinationsTotal = setTotal { prefs.getVaccinationsTotal() }
    val vaccinationsIncrease = setIncrease { prefs.getVaccinationsIncrease() }
    val vaccinationsIncreaseDate = setIncreaseDate { prefs.getVaccinationsIncreaseDate() }

    val confirmedCasesTotal = setTotal { prefs.getConfirmedCasesTotal() }
    val confirmedCasesIncrease = setIncrease { prefs.getConfirmedCasesIncrease() }
    val confirmedCasesIncreaseDate = setIncreaseDate { prefs.getConfirmedCasesIncreaseDate() }

    val activeCasesTotal = setTotal { prefs.getActiveCasesTotal() }
    val curedTotal = setTotal { prefs.getCuredTotal() }
    val deceasedTotal = setTotal { prefs.getDeceasedTotal() }
    val currentlyHospitalizedTotal = setTotal { prefs.getCurrentlyHospitalizedTotal() }

    // metrics
    val activationsTotal = setTotal { prefs.getActivationsTotal() }
    val activationsYesterday = setIncrease { prefs.getActivationsYesterday() }

    val keyPublishersTotal = setTotal { prefs.getKeyPublishersTotal() }
    val keyPublishersYesterday = setIncrease { prefs.getKeyPublishersYesterday() }

    val notificationsTotal = setTotal { prefs.getNotificationsTotal() }
    val notificationsYesterday = setIncrease { prefs.getNotificationsYesterday() }

    var lastMetricsIncreaseDate = setLastMetricsUpdateDate { prefs.getLastMetricsUpdate() }

    fun getMeasuresUrl() = AppConfig.currentMeasuresUrl

    private fun getStats(date: String? = null) {
        isLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                return@runCatching firebaseFunctionsRepository.getStats(date)
            }.onSuccess { res ->
                L.d(res.toString())
                isLoading.value = false

                /* Tests */
                safeLetAndSet(responseTotal = res.testsTotal,
                    responseIncrease = res.testsIncrease,
                    responseIncreaseDate = res.testsIncreaseDate,
                    liveTotal = testsTotal,
                    liveIncrease = testsIncrease,
                    liveIncreaseDate = testsIncreaseDate,
                    funTotal = { prefs.setTestsTotal(it) },
                    funIncrease = { prefs.setTestsIncrease(it) },
                    funIncreaseDate = { prefs.setTestsIncreaseDate(it) },
                )

                /* Antigen Tests */
                safeLetAndSet(res.antigenTestsTotal,
                    res.antigenTestsIncrease,
                    res.antigenTestsIncreaseDate,
                    antigenTestsTotal,
                    antigenTestsIncrease,
                    antigenTestsIncreaseDate,
                    { prefs.setAntigenTestsTotal(it) },
                    { prefs.setAntigenTestsIncrease(it) },
                    { prefs.setAntigenTestsIncreaseDate(it) },
                )

                /* Vaccination */
                safeLetAndSet(res.vaccinationsTotal,
                    res.vaccinationsIncrease,
                    res.vaccinationsIncreaseDate,
                    vaccinationsTotal,
                    vaccinationsIncrease,
                    vaccinationsIncreaseDate,
                    { prefs.setVaccinationsTotal(it) },
                    { prefs.setVaccinationsIncrease(it) },
                    { prefs.setVaccinationsIncreaseDate(it) },
                )

                /* Confirmed cases */
                safeLetAndSet(res.confirmedCasesTotal,
                    res.confirmedCasesIncrease,
                    res.confirmedCasesIncreaseDate,
                    confirmedCasesTotal,
                    confirmedCasesIncrease,
                    confirmedCasesIncreaseDate,
                    { prefs.setConfirmedCasesTotal(it) },
                    { prefs.setConfirmedCasesIncrease(it) },
                    { prefs.setConfirmedCasesIncreaseDate(it) },
                )

                /* Active cases */
                safeLetAndSet(responseTotal = res.activeCasesTotal,
                    liveTotal = activeCasesTotal,
                    funTotal = { prefs.setActiveCasesTotal(it) }
                )

                /* Cured */
                safeLetAndSet(res.curedTotal,
                    curedTotal,
                    { prefs.setCuredTotal(it) }
                )

                /* Deceased */
                safeLetAndSet(res.deceasedTotal,
                    deceasedTotal,
                    { prefs.setDeceasedTotal(it) }
                )

                /* Hospitalized */
                safeLetAndSet(res.currentlyHospitalizedTotal,
                    currentlyHospitalizedTotal,
                    { prefs.setCurrentlyHospitalizedTotal(it) }
                )

                /* Last stats update date */
                safeLetAndSetStatsDate(responseDate = res.date,
                    funLastUpdateDate = { prefs.setLastStatsUpdate(it) }
                )

            }.onFailure {
                onFailure(it)
            }
        }
    }

    private fun getMetrics() {
        isLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                return@runCatching firebaseFunctionsRepository.getDownloadMetrics()
            }.onSuccess { res ->
                L.d(res.toString())
                isLoading.value = false

                /* Activations */
                safeLetAndSet(
                    res.activationsTotal,
                    res.activationsYesterday,
                    activationsTotal,
                    activationsYesterday,
                    { prefs.setActivationsTotal(it) },
                    { prefs.setActivationsYesterday(it) },
                )

                /* Positive people whoa anonymously notified others */
                safeLetAndSet(
                    res.keyPublishersTotal,
                    res.keyPublishersYesterday,
                    keyPublishersTotal,
                    keyPublishersYesterday,
                    { prefs.setKeyPublishersTotal(it) },
                    { prefs.setKeyPublishersYesterday(it) },
                )

                /* Sent notifications about risky encounters */
                safeLetAndSet(
                    res.notificationsTotal,
                    res.notificationsYesterday,
                    notificationsTotal,
                    notificationsYesterday,
                    { prefs.setNotificationsTotal(it) },
                    { prefs.setNotificationsYesterday(it) },
                )

                /* Last metrics update date */
                safeLetAndSetMetricsDate(responseDate = res.date,
                    liveIncreaseDate = lastMetricsIncreaseDate,
                    funLastUpdateDate = { prefs.setLastMetricsUpdate(it) }
                )
            }.onFailure {
                onFailure(it)
            }
        }
    }

    /**
     * Handle failures.
     */
    private fun onFailure(it: Throwable) {
        isLoading.value = false
        if (it is ApiException) {
            L.e(it.status.toString() + " " + it.message)
        } else {
            L.e(it)
        }
    }
}