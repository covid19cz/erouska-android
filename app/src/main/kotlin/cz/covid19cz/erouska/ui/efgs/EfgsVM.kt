package cz.covid19cz.erouska.ui.efgs

import androidx.hilt.lifecycle.ViewModelInject
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.efgs.event.EfgsCommandEvent

class EfgsVM @ViewModelInject constructor() : BaseVM() {

    fun turnOnEfgs() {
        publish(EfgsCommandEvent(EfgsCommandEvent.Command.TURN_ON))
    }

    fun turnOffEfgs() {
        publish(EfgsCommandEvent(EfgsCommandEvent.Command.TURN_OFF))
    }

}