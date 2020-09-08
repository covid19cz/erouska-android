package cz.covid19cz.erouska.ui.permissions.location

import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.permissions.location.event.LocationEvent

class LocationOffVM : BaseVM() {

    fun turnOn() {
        publish(LocationEvent(LocationEvent.Command.ENABLE_LOCATION))
    }

}