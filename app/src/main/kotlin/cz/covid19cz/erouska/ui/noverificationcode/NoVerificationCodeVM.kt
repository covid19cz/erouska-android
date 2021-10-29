package cz.covid19cz.erouska.ui.noverificationcode

import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.noverificationcode.event.WriteEmailEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoVerificationCodeVM @Inject constructor() :
    BaseVM() {

    fun writeEmail() {
        publish(WriteEmailEvent())
    }
}