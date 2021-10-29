package cz.covid19cz.erouska.ui.welcome

import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.welcome.event.WelcomeCommandEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WelcomeVM @Inject constructor(private val prefs: SharedPrefsRepository) : BaseVM() {

    fun nextStep() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.VERIFY_APP))
    }

    fun help() {
        publish(WelcomeCommandEvent(WelcomeCommandEvent.Command.HELP))
    }
}
