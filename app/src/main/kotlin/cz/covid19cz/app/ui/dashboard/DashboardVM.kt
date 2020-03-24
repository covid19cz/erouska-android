package cz.covid19cz.app.ui.dashboard

import android.net.Uri
import androidx.lifecycle.Observer
import arch.livedata.SafeMutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.db.export.CsvExporter
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.app.ui.sandbox.ExportEvent
import io.reactivex.disposables.Disposable
import java.io.File

class DashboardVM(
    val bluetoothRepository: BluetoothRepository,
    private val exporter: CsvExporter,
    val prefs: SharedPrefsRepository
) : BaseVM() {

    private var exportDisposable: Disposable? = null
    private val storage = Firebase.storage
    val serviceRunning = SafeMutableLiveData(false)

    private val serviceObserver = Observer<Boolean> { isRunning ->
        if (!isRunning && !prefs.getAppPaused()) {
                publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_ON))
        }
    }

    override fun onCleared() {
        serviceRunning.removeObserver(serviceObserver)
        super.onCleared()
    }

    fun init() {
        serviceRunning.observeForever(serviceObserver)
    }

    fun pause() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_OFF))
    }

    fun start() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_ON))
    }

    fun share() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.SHARE))
    }

    fun export() {
        exportDisposable?.dispose()
        exportDisposable = exporter.export().subscribe({
            uploadToStorage(it)
            publish(ExportEvent.Complete(it))
        }, {
            publish(ExportEvent.Error(it.message ?: "Export failed"))
        }
        )
    }

    private fun uploadToStorage(path: String) {
        val fuid = FirebaseAuth.getInstance().uid
        val timestamp = System.currentTimeMillis()
        val ref = storage.reference.child("proximity/$fuid/$timestamp.csv")
        val metadata = storageMetadata {
            contentType = "text/csv"
            setCustomMetadata("version", AppConfig.CSV_VERSION.toString())
            setCustomMetadata("buid", prefs.getDeviceBuid())
        }
        ref.putFile(Uri.fromFile(File(path)), metadata).addOnSuccessListener {
            publish(ExportEvent.Complete("Upload success"))
        }.addOnFailureListener {
            publish(ExportEvent.Error(it.message ?: "Upload failed"))
        }
    }
}