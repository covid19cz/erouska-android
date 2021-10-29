package cz.covid19cz.erouska.ui.about

import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.base.UrlEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutVM @Inject constructor() : BaseVM() {

    fun tosLinkClicked() {
        publish(UrlEvent(AppConfig.conditionsOfUseUrl))
    }
}