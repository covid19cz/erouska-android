package cz.covid19cz.app.ui.dashboard

import androidx.lifecycle.Observer
import arch.livedata.SafeMutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.db.export.CsvExporter
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.dashboard.event.DashboardCommandEvent
import io.reactivex.disposables.Disposable

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
}