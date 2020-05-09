package cz.covid19cz.erouska.screenObject

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.helpers.click

object NavigationPanelScreen {

    fun goToMyDataTab() = click(R.id.nav_my_data)

    fun goToHomeTab() = click(R.id.nav_dashboard)

    fun goToContactsTab() = click(R.id.nav_contacts)
}