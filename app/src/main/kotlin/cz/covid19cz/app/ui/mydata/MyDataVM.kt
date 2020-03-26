package cz.covid19cz.app.ui.mydata

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.ScanDataEntity
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.utils.L
import java.text.SimpleDateFormat
import java.util.*

class MyDataVM(
    val dbRepo: DatabaseRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val allItems = ObservableArrayList<ScanDataEntity>()
    val criticalItems = ObservableArrayList<ScanDataEntity>()
    private val dateFormatter = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val todayCount = SafeMutableLiveData(0)
    val allCount = SafeMutableLiveData(0)
    val allCriticalCount = SafeMutableLiveData(0)

    val descriptionVisible = SafeMutableLiveData(true)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        subscribeToDb()
    }

    fun subscribeToDb() {

        val todayBeginCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        subscribe(dbRepo.getAllDesc(), {
            L.e(it)
        }) { newData ->
            allItems.clear()
            allItems.addAll(newData)
        }

        subscribe(dbRepo.getCriticalDesc(), {
            L.e(it)
        }) { newData ->
            criticalItems.clear()
            criticalItems.addAll(newData)
        }

        subscribe(dbRepo.getBuidCount(todayBeginCalendar.timeInMillis), { L.e(it) }) {
            todayCount.value = it
        }

        subscribe(dbRepo.getBuidCount(0), { L.e(it) }) {
            allCount.value = it
        }

        subscribe(dbRepo.getCriticalBuidCount(0), { L.e(it) }) {
            allCriticalCount.value = it
        }
    }

    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormatter.format(Date(timestamp))
    }

    fun sendData() {
        val minutesSinceLastUpload = (System.currentTimeMillis() - prefs.getLastUploadTimestamp()) / 60000
        if (minutesSinceLastUpload < AppConfig.uploadWaitingMinutes) {
            publish(ExportEvent.PleaseWait(AppConfig.uploadWaitingMinutes - minutesSinceLastUpload.toInt()))
        } else {
            publish(ExportEvent.Confirmation)
        }
    }

    fun toggleDescription() {
        descriptionVisible.postValue(!descriptionVisible.value)
    }
}
