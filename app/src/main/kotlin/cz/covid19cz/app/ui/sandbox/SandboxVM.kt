package cz.covid19cz.app.ui.sandbox

import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
    import arch.livedata.SafeMutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.R
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.db.export.CsvExporter
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.dashboard.event.DashboardCommandEvent
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.io.File

class SandboxVM(
    val bluetoothRepository: BluetoothRepository,
    private val exporter: CsvExporter,
    private val prefs : SharedPrefsRepository,
    private val repository: DatabaseRepository
) :
    BaseVM() {

    val buid = prefs.getDeviceBuid()
    val devices = bluetoothRepository.scanResultsList
    val serviceRunning = SafeMutableLiveData(false)
    val power = SafeMutableLiveData(0)
    var exportDisposable: Disposable? = null
    val storage = Firebase.storage
    val advertisingSupportText = MutableLiveData<String>().apply {
        value = if (bluetoothRepository.supportsAdvertising()){
            "Podporuje vysílání"
        } else {
            "Nepodporuje vysílání"
        }
    }

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

    fun start() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_ON))
    }

    fun confirmStart() {
        serviceRunning.value = true
    }

    fun stop() {
        serviceRunning.value = false
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_OFF))
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
        navigate(R.id.action_nav_sandbox_to_nav_my_data)
    }

    fun nuke() {
        prefs.clear()
        Completable.fromAction(repository::clear)
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                navigate(R.id.action_nav_sandbox_to_nav_welcome_fragment)
            }
        FirebaseAuth.getInstance().signOut()
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

}