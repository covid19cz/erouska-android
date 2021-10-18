package cz.covid19cz.erouska.ui.update.playservices

import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.update.playservices.event.UpdatePlayServicesEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdatePlayServicesVM @Inject constructor() : BaseVM() {

    fun openPlayStore() {
        publish(UpdatePlayServicesEvent(UpdatePlayServicesEvent.Command.PLAY_STORE))
    }

}