package cz.covid19cz.erouska.ui.about

import androidx.hilt.lifecycle.ViewModelInject
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.base.UrlEvent

class AboutVM @ViewModelInject constructor() : BaseVM() {

    fun tosLinkClicked() {
        publish(UrlEvent(AppConfig.conditionsOfUseUrl))
    }

}