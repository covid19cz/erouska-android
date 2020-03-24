package cz.covid19cz.app.ui.main

import arch.livedata.SafeMutableLiveData
import cz.covid19cz.app.ui.base.BaseVM

class MainVM: BaseVM() {

    val serviceRunning = SafeMutableLiveData(false)

}