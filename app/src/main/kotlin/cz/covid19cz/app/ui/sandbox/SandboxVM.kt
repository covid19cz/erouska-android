package cz.covid19cz.app.ui.sandbox

import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.app.db.export.CsvExporter
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.bt.entity.ScanSession
import cz.covid19cz.app.ui.sandbox.event.ServiceCommandEvent
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.utils.Log
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class SandboxVM(val bluetoothRepository : BluetoothRepository, val exporter: CsvExporter) : BaseVM() {

    val deviceId = SafeMutableLiveData("")
    val devices = bluetoothRepository.scanResultsList
    val serviceRunning = SafeMutableLiveData(false)
    val power = SafeMutableLiveData(0)
    var scanDisposable : Disposable? = null
    var exportDisposable: Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){

    }

    override fun onCleared() {
        super.onCleared()
        exportDisposable?.dispose()
    }

    fun refreshData() : MutableCollection<ScanSession>{
        val devices = bluetoothRepository.scanResultsMap.values
        for (device in devices) {
            device.calculate()
        }
        return devices
    }

    fun onError(t : Throwable){
        Log.e(t)
    }

    fun start(){
        publish(ServiceCommandEvent(ServiceCommandEvent.Command.TURN_ON))
        scanDisposable?.dispose()
        scanDisposable = subscribe(Observable.interval(0,10, TimeUnit.SECONDS).map {
            val devices = bluetoothRepository.scanResultsList
            for (device in devices) {
               device.checkOutOfRange()
            }
            return@map devices
        }, this::onError){

        }
    }

    fun confirmStart(){
        serviceRunning.value = true
    }

    fun stop(){
        serviceRunning.value = false
        scanDisposable?.dispose()
        scanDisposable = null
        publish(ServiceCommandEvent(ServiceCommandEvent.Command.TURN_OFF))
    }

    fun export() {
        exportDisposable?.dispose()
        exportDisposable = exporter.export().subscribe (
            {
                publish(ExportEvent.Complete(it))
            },
            {
                publish(ExportEvent.Error(it.message ?: "Export failed"))
            }
        )
    }

    fun powerToString(pwr : Int) : String{
        return when(pwr){
            0 -> "REMOTE_CONFIG"
            1 -> "ULTRA_LOW"
            2 -> "LOW"
            3 -> "MEDIUM"
            4 -> "HIGH"
            else -> "UNKNOWN"
        }
    }

}