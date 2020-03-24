package cz.covid19cz.app.ui.mydata

import android.net.Uri
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.ScanDataEntity
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.db.export.CsvExporter
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.utils.L
import io.reactivex.disposables.Disposable
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MyDataVM(
    val dbRepo: DatabaseRepository,
    private val exporter: CsvExporter,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val items = ObservableArrayList<ScanDataEntity>()
    private val dateFormatter = SimpleDateFormat("d.M. HH:mm", Locale.getDefault())
    val todayCount = SafeMutableLiveData(0)
    val todayCritical = SafeMutableLiveData(0)
    val twoWeeksCount = SafeMutableLiveData(0)
    val twoWeeksCriticalCount = SafeMutableLiveData(0)
    var exportDisposable: Disposable? = null
    private val storage = Firebase.storage

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

        subscribe(dbRepo.getBuidCount(todayBeginCalendar.timeInMillis), { L.e(it) }) {
            todayCount.value = it
        }

        subscribe(
            dbRepo.getCriticalExpositions(
                todayBeginCalendar.timeInMillis,
                AppConfig.criticalExpositionRssi,
                AppConfig.criticalExpositionMinutes
            ), { L.e(it) }) {
            todayCritical.value = it.size
        }

        subscribe(dbRepo.getBuidCount(twoWeeksBefore.timeInMillis), { L.e(it) }) {
            twoWeeksCount.value = it
        }

        subscribe(
            dbRepo.getCriticalExpositions(
                twoWeeksBefore.timeInMillis,
                AppConfig.criticalExpositionRssi,
                AppConfig.criticalExpositionMinutes
            ), { L.e(it) }) { result ->
            result.forEach {
                L.d("${it.buid} - ${it.expositionTime / 1000}s")
            }
            twoWeeksCriticalCount.value = result.size
        }

    }

    override fun onCleared() {
        super.onCleared()
        exportDisposable?.dispose()
    }

    fun formatTimeStamps(time: Long): String {
        return dateFormatter.format(Date(time))
    }

    fun getAvgSeconds(start: Long, end: Long, count: Int): String {
        return String.format("%.1fs", (end - start) / count.toFloat() / 1000f)
    }

    fun sendData() {
        val minutesSinceLastUpload = (System.currentTimeMillis() - prefs.getLastUploadTimestamp())/60000
        if (minutesSinceLastUpload < AppConfig.uploadWaitingMinutes) {
            publish(ExportEvent.PleaseWait(AppConfig.uploadWaitingMinutes - minutesSinceLastUpload.toInt()))
        } else {
            publish(ExportEvent.Confirmation)
        }
    }

    fun confirmSendingData() {
        exportDisposable?.dispose()
        exportDisposable = exporter.export(prefs.getLastUploadTimestamp()).subscribe({
            uploadToStorage(it)
        }, {
            L.e(it)
            publish(ExportEvent.Error(it.message ?: "Export failed"))
        }
        )
    }

    private fun uploadToStorage(path: String) {
        val fuid = FirebaseAuth.getInstance().uid
        val timestamp = System.currentTimeMillis()
        val buid = prefs.getDeviceBuid()
        val ref = storage.reference.child("proximity/$fuid/$buid/$timestamp.csv")
        val metadata = storageMetadata {
            contentType = "text/csv"
            setCustomMetadata("version", "3")
        }
        ref.putFile(Uri.fromFile(File(path)), metadata).addOnSuccessListener {
            prefs.saveLastUploadTimestamp(timestamp)
            publish(ExportEvent.Complete)
        }.addOnFailureListener {
            L.e(it)
            publish(ExportEvent.Error(it.message ?: "Upload failed"))
        }
    }
}