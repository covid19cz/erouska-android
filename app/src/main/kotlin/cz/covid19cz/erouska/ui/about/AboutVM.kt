package cz.covid19cz.erouska.ui.about

import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.base.UrlEvent

class AboutVM : BaseVM() {

    fun tosLinkClicked() {
        publish(UrlEvent(AppConfig.conditionsOfUseUrl))
    }

}