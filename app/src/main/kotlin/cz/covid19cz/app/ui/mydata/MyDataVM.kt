package cz.covid19cz.app.ui.mydata

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.ScanDataEntity
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.utils.L
import java.text.SimpleDateFormat
import java.util.*

class MyDataVM(val dbRepo: DatabaseRepository) : BaseVM() {

    val items = ObservableArrayList<ScanDataEntity>()
    val dateFormatter = SimpleDateFormat("d.M. HH:mm", Locale.getDefault())
    val todayCount = SafeMutableLiveData(0)
    val todayCritical = SafeMutableLiveData(0)
    val twoWeeksCount = SafeMutableLiveData(0)
    val twoWeeksCriticalCount = SafeMutableLiveData(0)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        subscribe(dbRepo.data.map { orig ->
            orig.sortedByDescending { it.timestampStart }
        }, { L.e(it) }) {
            items.addAll(it)
        }

        val todayBeginCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val twoWeeksBefore = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -14)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        subscribe(dbRepo.getBuidCount(todayBeginCalendar.timeInMillis),{ L.e(it) }) {
            todayCount.value = it
        }

        subscribe(dbRepo.getCriticalExpositions(todayBeginCalendar.timeInMillis, AppConfig.criticalExpositionRssi, AppConfig.criticalExpositionMinutes),{ L.e(it) }) {
            todayCritical.value = it.size
        }

        subscribe(dbRepo.getBuidCount(twoWeeksBefore.timeInMillis),{ L.e(it) }) {
            twoWeeksCount.value = it
        }

        subscribe(dbRepo.getCriticalExpositions(twoWeeksBefore.timeInMillis, AppConfig.criticalExpositionRssi, AppConfig.criticalExpositionMinutes), { L.e(it) }) { result ->
            result.forEach {
                L.d("${it.buid} - ${it.expositionTime / 1000}s")
            }
            twoWeeksCriticalCount.value = result.size
        }

    }

    fun formatTimeStamps(time: Long): String {
        return dateFormatter.format(Date(time))
    }

    fun getAvgSeconds(start: Long, end: Long, count: Int): String {
        return String.format("%.1fs", (end - start) / count.toFloat() / 1000f)
    }

    fun sendData() {
        // TODO
    }
}