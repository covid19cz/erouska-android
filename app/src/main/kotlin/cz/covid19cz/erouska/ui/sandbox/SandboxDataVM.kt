package cz.covid19cz.erouska.ui.sandbox

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.exposurenotification.DailySummary
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.daysSinceEpochToDateString
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SandboxDataVM(private val exposureNotificationsRepository: ExposureNotificationsRepository, val prefs : SharedPrefsRepository) : BaseVM() {

    val dailySummaries = ObservableArrayList<DailySummary>()
    val exposureWindows = ObservableArrayList<Any>()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(){
        getDailySummaries()
        getExposureWindows()
    }

    fun getExposureWindows() {
        exposureWindows.clear()
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.getExposureWindows().sortedByDescending { it.dateMillisSinceEpoch }
            }.onSuccess {
                it.forEach {
                    exposureWindows.add(it)
                    exposureWindows.add("HEADER")
                    it.scanInstances.forEach {
                        exposureWindows.add(it)
                    }
                }
                exposureWindows.addAll(it)
            }.onFailure {
                L.e(it)
            }
        }
    }

    fun getDailySummaries() {
        dailySummaries.clear()
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.getDailySummaries().sortedByDescending { it.daysSinceEpoch }
            }.onSuccess {
                dailySummaries.addAll(it)
            }.onFailure {
                L.e(it)
            }
        }
    }

    fun daysToString(daysSinceEpoch: Int): String {
        return daysSinceEpoch.daysSinceEpochToDateString()
    }

    fun dateInMilisToString(milis: Long): String {
        val formatter = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = milis
        }
        return formatter.format(dateTime.time)
    }

}
