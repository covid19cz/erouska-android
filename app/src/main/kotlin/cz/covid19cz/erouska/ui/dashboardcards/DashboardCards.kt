package cz.covid19cz.erouska.ui.dashboardcards

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentDashboardCardsBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dashboard_cards.*

@AndroidEntryPoint
class DashboardCards :
    BaseFragment<FragmentDashboardCardsBinding, DashboardCardsVM>(R.layout.fragment_dashboard_cards, DashboardCardsVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)

        dash_card_risky_encounter.card_title = AppConfig.noEncounterHeader
        dash_card_risky_encounter.card_subtitle = AppConfig.noEncounterBody
    }
}
