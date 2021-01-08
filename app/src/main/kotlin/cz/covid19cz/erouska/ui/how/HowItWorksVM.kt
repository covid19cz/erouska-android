package cz.covid19cz.erouska.ui.how

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.how.event.HowItWorksEvent

class HowItWorksVM @ViewModelInject constructor(private val prefs: SharedPrefsRepository) :
    BaseVM() {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        prefs.setHowItWorksShown()
    }

    fun writeEmail() {
        publish(HowItWorksEvent(HowItWorksEvent.Command.WRITE_EMAIL))
    }

    fun close() {
        publish(HowItWorksEvent(HowItWorksEvent.Command.CLOSE))
    }
}