package cz.covid19cz.erouska.ui.noverificationcode

import androidx.hilt.lifecycle.ViewModelInject
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.noverificationcode.event.WriteEmailEvent

class NoVerificationCodeVM @ViewModelInject constructor() :
    BaseVM() {

    fun writeEmail() {
        publish(WriteEmailEvent())
    }
}