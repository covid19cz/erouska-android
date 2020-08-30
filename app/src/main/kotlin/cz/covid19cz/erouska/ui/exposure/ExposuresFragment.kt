package cz.covid19cz.erouska.ui.exposure

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentExposuresBinding
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.exposure.event.ExposuresCommandEvent
import kotlinx.android.synthetic.main.fragment_exposures.*

class ExposuresFragment : BaseFragment<FragmentExposuresBinding, ExposuresVM>(
    R.layout.fragment_exposures,
    ExposuresVM::class
) {

    val args : ExposuresFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true)

        activity?.title = AppConfig.exposureUITitle

        symptoms_text.text = AppConfig.symptomsUITitle
        spread_text.text = AppConfig.spreadPreventionUITitle
        earlier_exposures_text.text = AppConfig.recentExposuresUITitle
        symptoms_content.text = AppConfig.riskyEncountersWithSymptoms
        no_symptoms_content.text = AppConfig.riskyEncountersWithoutSymptoms

        viewModel.checkExposures(args.demo)

        subscribe(ExposuresCommandEvent::class) {
            when (it.command) {
                ExposuresCommandEvent.Command.NO_RECENT_EXPOSURES -> onNoRecentExposures()
                ExposuresCommandEvent.Command.RECENT_EXPOSURE -> onRecentExposures()
            }
        }

        setupListeners()
    }

    private fun setupListeners() {
        symptoms_container.setOnClickListener { navigate(R.id.action_nav_dashboard_to_nav_main_symptoms) }
        spread_prevention_container.setOnClickListener { navigate(R.id.action_nav_dashboard_to_nav_spread_prevention) }
        earlier_exposures_container.setOnClickListener { navigate(R.id.action_nav_dashboard_to_nav_recent_exposures) }
    }

    private fun onNoRecentExposures() {
        no_exposures_header.text = AppConfig.noEncounterHeader
        no_exposures_body.text = AppConfig.noEncounterBody

        no_exposures_img.show()
        no_exposures_header.show()
        no_exposures_body.show()

        last_exposure.hide()

        divider_1.hide()
        divider_2.hide()
        symptoms_header.hide()
        symptoms_content.hide()
        no_symptoms_header.hide()
        no_symptoms_content.hide()

        symptoms_container.hide()
        spread_prevention_container.hide()
        earlier_exposures_container.hide()
        bottom_separator.hide()
    }

    private fun onRecentExposures() {
        last_exposure.show()

        divider_1.show()
        divider_2.show()
        symptoms_header.show()
        symptoms_content.show()
        no_symptoms_header.show()
        no_symptoms_content.show()

        symptoms_container.show()
        spread_prevention_container.show()
        earlier_exposures_container.show()

        no_exposures_img.hide()
        no_exposures_header.hide()
        no_exposures_body.hide()
    }

}
