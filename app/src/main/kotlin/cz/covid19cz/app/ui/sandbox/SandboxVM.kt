package cz.covid19cz.app.ui.sandbox

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import arch.viewmodel.BaseArchViewModel
import com.polidea.rxandroidble2.scan.ScanResult
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.sandbox.entity.ScanResultEntity
import cz.covid19cz.app.ui.sandbox.event.ServiceCommandEvent
import cz.covid19cz.app.utils.BtUtils
import cz.covid19cz.app.utils.Log
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class SandboxVM : BaseVM() {

    val deviceId = SafeMutableLiveData("")
    val devices = BtUtils.scanResultsList
    val serviceRunning = SafeMutableLiveData(false)
    val power = SafeMutableLiveData(1)
    var scanDisposable : Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){

    }

    fun refreshData() : MutableCollection<ScanResultEntity>{
        val devices = BtUtils.scanResultsMap.values
        for (device in devices) {
            device.recalculate()
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
            val devices = BtUtils.scanResultsList
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

    fun powerToString(pwr : Int) : String{
        return when(pwr){
            0 -> "ULTRA_LOW"
            1 -> "LOW"
            2 -> "MEDIUM"
            3 -> "HIGH"
            else -> "UNKNOWN"
        }
    }

}