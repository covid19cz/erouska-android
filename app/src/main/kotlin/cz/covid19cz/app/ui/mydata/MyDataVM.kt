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

    val allItems = ObservableArrayList<ScanDataEntity>()
    val criticalItems = ObservableArrayList<ScanDataEntity>()
    private val dateFormatter = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val todayCount = SafeMutableLiveData(0)
    val allCount = SafeMutableLiveData(0)
    val allCriticalCount = SafeMutableLiveData(0)
    val currentTab = SafeMutableLiveData(0)
    var exportDisposable: Disposable? = null
    private val storage = Firebase.storage

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

    override fun onCleared() {
        super.onCleared()
        exportDisposable?.dispose()
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
            setCustomMetadata("version", AppConfig.CSV_VERSION.toString())
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