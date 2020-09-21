package cz.covid19cz.erouska.ui.welcome

import androidx.hilt.lifecycle.ViewModelInject
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.welcome.event.WelcomeCommandEvent

class WelcomeVM @ViewModelInject constructor(private val prefs: SharedPrefsRepository) : BaseVM() {

    fun nextStep() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.VERIFY_APP))
    }

    fun help() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.HELP))
    }

    fun wasAppUpdated(): Boolean {
        return prefs.isUpdateFromLegacyVersion()
    }
}
