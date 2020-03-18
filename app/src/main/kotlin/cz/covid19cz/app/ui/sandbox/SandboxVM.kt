package cz.covid19cz.app.ui.sandbox

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.bt.entity.ScanSession
import cz.covid19cz.app.ui.sandbox.event.ServiceCommandEvent
import cz.covid19cz.app.bt.BluetoothRepository
import cz.covid19cz.app.utils.Log
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class SandboxVM(val bluetoothRepository : BluetoothRepository) : BaseVM() {

    val deviceId = SafeMutableLiveData("")
    val devices = bluetoothRepository.scanResultsList
    val serviceRunning = SafeMutableLiveData(false)
    val power = SafeMutableLiveData(0)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){

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
    }

    fun confirmStart(){
        serviceRunning.value = true
    }

    fun stop(){
        serviceRunning.value = false
        publish(ServiceCommandEvent(ServiceCommandEvent.Command.TURN_OFF))
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