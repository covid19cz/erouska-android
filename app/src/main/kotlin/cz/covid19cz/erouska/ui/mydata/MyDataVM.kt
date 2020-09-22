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
        const val LAST_UPDATE_UI_FORMAT = "dd.MM.yyyy" // date format used in UI
        const val LAST_UPDATE_API_FORMAT = "yyyyMMdd" // date format returned from API
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (!DateUtils.isToday(prefs.getLastStatsUpdate())) {
            getStats()
        }
    }

    fun measures() {
        publish(MyDataCommandEvent(MyDataCommandEvent.Command.MEASURES))
    }

    val testsTotal = SafeMutableLiveData(prefs.getTestsTotal())
    val testsIncrease = SafeMutableLiveData(prefs.getTestsIncrease())

    val confirmedCasesTotal = SafeMutableLiveData(prefs.getConfirmedCasesTotal())
    val confirmedCasesIncrease = SafeMutableLiveData(prefs.getConfirmedCasesIncrease())

    val activeCasesTotal = SafeMutableLiveData(prefs.getActiveCasesTotal())
    val activeCasesIncrease = SafeMutableLiveData(prefs.getActiveCasesIncrease())

    val curedTotal = SafeMutableLiveData(prefs.getCuredTotal())
    val curedIncrease = SafeMutableLiveData(prefs.getCuredIncrease())

    val deceasedTotal = SafeMutableLiveData(prefs.getDeceasedTotal())
    val deceasedIncrease = SafeMutableLiveData(prefs.getDeceasedIncrease())

    val currentlyHospitalizedTotal = SafeMutableLiveData(prefs.getCurrentlyHospitalizedTotal())
    val currentlyHospitalizedIncrease =
        SafeMutableLiveData(prefs.getCurrentlyHospitalizedIncrease())

    var lastUpdate = if (prefs.getLastStatsUpdate() == 0L) {
        SafeMutableLiveData("-")
    } else {
        SafeMutableLiveData(
            SimpleDateFormat(
                LAST_UPDATE_UI_FORMAT,
                Locale.getDefault()
            ).format(Date(prefs.getLastStatsUpdate()))
        )
    }

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
                safeLet(
                    response.activeCasesTotal,
                    response.activeCasesIncrease
                ) { total, increase ->
                    activeCasesTotal.value = total
                    activeCasesIncrease.value = increase

                    prefs.setActiveCasesTotal(total)
                    prefs.setActiveCasesIncrease(increase)
                }
                safeLet(response.curedTotal, response.curedIncrease) { total, increase ->
                    curedTotal.value = total
                    curedIncrease.value = increase

                    prefs.setCuredTotal(total)
                    prefs.setCuredIncrease(increase)
                }
                safeLet(response.deceasedTotal, response.deceasedIncrease) { total, increase ->
                    deceasedTotal.value = total
                    deceasedIncrease.value = increase

                    prefs.setDeceasedTotal(total)
                    prefs.setDeceasedIncrease(increase)
                }
                safeLet(
                    response.currentlyHospitalizedTotal,
                    response.currentlyHospitalizedIncrease
                ) { total, increase ->
                    currentlyHospitalizedTotal.value = total
                    currentlyHospitalizedIncrease.value = increase

                    prefs.setCurrentlyHospitalizedTotal(total)
                    prefs.setCurrentlyHospitalizedIncrease(increase)
                }

                response.date?.let {
                    val lastUpdateDate = SimpleDateFormat(
                        LAST_UPDATE_API_FORMAT,
                        Locale.getDefault()
                    ).parse(response.date)

                    lastUpdateDate?.time?.let { lastUpdateMillis ->
                        prefs.setLastStatsUpdate(lastUpdateMillis)

                        lastUpdate.value = SimpleDateFormat(
                            LAST_UPDATE_UI_FORMAT,
                            Locale.getDefault()
                        ).format(Date(lastUpdateMillis))
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

