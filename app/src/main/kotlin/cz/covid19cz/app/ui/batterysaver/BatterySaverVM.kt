package cz.covid19cz.app.ui.batterysaver

import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.batterysaver.event.BatterSaverCommandEvent
import cz.covid19cz.app.ui.batterysaver.event.BatterSaverCommandEvent.Command.DISABLE_BATTER_SAVER

class BatterySaverVM : BaseVM() {

    fun disableBatterySaver(){
        publish(BatterSaverCommandEvent(DISABLE_BATTER_SAVER))
    }
}