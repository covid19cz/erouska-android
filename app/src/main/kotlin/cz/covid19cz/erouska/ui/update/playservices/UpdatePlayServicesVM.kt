package cz.covid19cz.erouska.ui.update.playservices

import androidx.hilt.lifecycle.ViewModelInject
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.update.playservices.event.UpdatePlayServicesEvent

class UpdatePlayServicesVM @ViewModelInject constructor() : BaseVM() {

    fun openPlayStore() {
        publish(UpdatePlayServicesEvent(UpdatePlayServicesEvent.Command.PLAY_STORE))
    }

}