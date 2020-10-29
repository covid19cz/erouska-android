package cz.covid19cz.erouska.ui.mydata

import android.text.format.DateUtils
import androidx.hilt.lifecycle.ViewModelInject
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
import cz.covid19cz.erouska.ui.mydata.event.MyDataCommandEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyDataVM @ViewModelInject constructor(
    private val firebaseFunctionsRepository: FirebaseFunctionsRepository,
    val prefs: SharedPrefsRepository
) : BaseVM() {

    companion object {
        const val LAST_UPDATE_API_FORMAT = "yyyyMMdd" // date format returned from API
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (!DateUtils.isToday(prefs.getLastStatsUpdate())) {
            getStats()
        }
//        if (!DateUtils.isToday(prefs.getLastMetricsUpdate())) {
            getMetrics()
//        }
    }

    fun measures() {
        publish(MyDataCommandEvent(MyDataCommandEvent.Command.MEASURES))
    }

    // stats
    val testsTotal = SafeMutableLiveData(prefs.getTestsTotal())
    val testsIncrease = SafeMutableLiveData(prefs.getTestsIncrease())
    val confirmedCasesTotal = SafeMutableLiveData(prefs.getConfirmedCasesTotal())
    val confirmedCasesIncrease = SafeMutableLiveData(prefs.getConfirmedCasesIncrease())
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

    fun getMeasuresUrl() = AppConfig.currentMeasuresUrl

    private fun getStats(date: String? = null) {
        viewModelScope.launch {
            kotlin.runCatching {
                return@runCatching firebaseFunctionsRepository.getStats(date)
            }.onSuccess { response ->
                L.d(response.toString())

                safeLet(response.testsTotal, response.testsIncrease) { total, increase ->
                    testsTotal.value = total
                    testsIncrease.value = increase

                    prefs.setTestsTotal(total)
                    prefs.setTestsIncrease(increase)
                }
                safeLet(
                    response.confirmedCasesTotal,
                    response.confirmedCasesIncrease
                ) { total, increase ->
                    confirmedCasesTotal.value = total
                    confirmedCasesIncrease.value = increase

                    prefs.setConfirmedCasesTotal(total)
                    prefs.setConfirmedCasesIncrease(increase)
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
                    val lastUpdateDate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(response.date)

                    lastUpdateDate?.time?.let { lastUpdateMillis ->
                        prefs.setLastStatsUpdate(lastUpdateMillis)
                    }
                }
            }.onFailure {
                if (it is ApiException) {
                    L.e(it.status.toString() + " " + it.message)
                } else {
                    L.e(it)
                }
            }
        }
    }

    private fun getMetrics() {
        viewModelScope.launch {
            kotlin.runCatching {
                return@runCatching firebaseFunctionsRepository.getDownloadMetrics()
            }.onSuccess { response ->
                L.d(response.toString())

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
                    val lastUpdateDate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(response.date)

                    lastUpdateDate?.time?.let { lastUpdateMillis ->
                        prefs.setLastMetricsUpdate(lastUpdateMillis)
                    }
                }
            }.onFailure {
                if (it is ApiException) {
                    L.e(it.status.toString() + " " + it.message)
                } else {
                    L.e(it)
                }
            }
        }
    }
}