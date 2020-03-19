package cz.covid19cz.app.ui.sandbox

import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.R
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.bt.entity.ScanSession
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.db.export.CsvExporter
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.sandbox.event.ServiceCommandEvent
import cz.covid19cz.app.utils.Log
import io.reactivex.disposables.Disposable
import java.io.File

class SandboxVM(val bluetoothRepository: BluetoothRepository, val exporter: CsvExporter, val prefs : SharedPrefsRepository) :
    BaseVM() {

    val buid = SafeMutableLiveData(prefs.getDeviceBuid() ?: "")
    val devices = bluetoothRepository.scanResultsList
    val serviceRunning = SafeMutableLiveData(false)
    val power = SafeMutableLiveData(0)
    var exportDisposable: Disposable? = null
    val storage = Firebase.storage

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        power.observeForever { value ->
            if (value == 0){
                // 0 means remote config
                AppConfig.overrideAdvertiseTxPower = null
            } else {
                // save overriding power setting (value-1)
                AppConfig.overrideAdvertiseTxPower = value-1
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exportDisposable?.dispose()
    }

    fun refreshData(): MutableCollection<ScanSession> {
        val devices = bluetoothRepository.scanResultsMap.values
        for (device in devices) {
            device.calculate()
        }
        return devices
    }

    fun onError(t: Throwable) {
        Log.e(t)
    }

    fun start() {
        publish(ServiceCommandEvent(ServiceCommandEvent.Command.TURN_ON))
    }

    fun confirmStart() {
        serviceRunning.value = true
    }

    fun stop() {
        serviceRunning.value = false
        publish(ServiceCommandEvent(ServiceCommandEvent.Command.TURN_OFF))
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

    fun openDbExplorer(){
        navigate(R.id.action_nav_sandbox_to_nav_db_explorer)
    }

    private fun uploadToStorage(path: String) {
        val fuid = FirebaseAuth.getInstance().uid
        val timestamp = System.currentTimeMillis()
        val ref = storage.reference.child("proximity/$fuid/$timestamp.csv")
        val metadata = storageMetadata {
            contentType = "text/csv"
            setCustomMetadata("version", "1")
        }
        ref.putFile(Uri.fromFile(File(path)), metadata).addOnSuccessListener {
            publish(ExportEvent.Complete("Upload success"))
        }.addOnFailureListener {
            publish(ExportEvent.Error(it.message ?: "Upload failed"))
        }
    }

    fun powerToString(pwr: Int): String {
        return when (pwr) {
            0 -> "REMOTE_CONFIG"
            1 -> "ULTRA_LOW"
            2 -> "LOW"
            3 -> "MEDIUM"
            4 -> "HIGH"
            else -> "UNKNOWN"
        }
    }

}