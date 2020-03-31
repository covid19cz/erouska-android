package cz.covid19cz.erouska.ui.sandbox

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.bt.BluetoothRepository
import cz.covid19cz.erouska.db.DatabaseRepository
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent

class SandboxVM(
    val bluetoothRepository: BluetoothRepository,
    private val prefs : SharedPrefsRepository,
    private val repository: DatabaseRepository
) :
    BaseVM() {

    val buid = prefs.getDeviceBuid()
    val devices = bluetoothRepository.scanResultsList
    val serviceRunning = SafeMutableLiveData(false)
    val power = SafeMutableLiveData(0)
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


    fun openDbExplorer(){
        navigate(R.id.action_nav_sandbox_to_nav_my_data)
    }



}