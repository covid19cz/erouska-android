package cz.covid19cz.erouska.ui.sandbox

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.exposurenotification.DailySummariesConfig
import com.google.android.gms.nearby.exposurenotification.DailySummary
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SandboxDataVM(private val exposureNotificationsRepository: ExposureNotificationsRepository, val prefs : SharedPrefsRepository) : BaseVM() {

    val dailySummaries = ObservableArrayList<DailySummary>()
    val exposureWindows = ObservableArrayList<ExposureWindow>()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(){
        getDailySummaries()
        getExposureWindows()
    }

    fun getExposureWindows() {
        exposureWindows.clear()
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.getExposureWindows()
            }.onSuccess {
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
                exposureNotificationsRepository.getDailySummaries()
            }.onSuccess {
                dailySummaries.addAll(it)
            }.onFailure {
                L.e(it)
            }
        }
    }

    fun daysToString(daysSinceEpoch: Int): String {
        val formatter = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (daysSinceEpoch.toLong() * 24 * 60 * 60 * 1000)
        }
        return formatter.format(dateTime.time)
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
