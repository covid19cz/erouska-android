package cz.covid19cz.erouska.ui.main

import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.ui.base.BaseVM

class MainVM: BaseVM() {

    val serviceRunning = SafeMutableLiveData(false)

}