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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_exposures.*

@AndroidEntryPoint
class ExposuresFragment : BaseFragment<FragmentExposuresBinding, ExposuresVM>(
    R.layout.fragment_exposures,
    ExposuresVM::class
) {

    val args: ExposuresFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.CLOSE)

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
        symptoms_container.setOnClickListener { safeNavigate(R.id.action_nav_exposure_to_nav_main_symptoms, R.id.nav_exposures) }
        spread_prevention_container.setOnClickListener { safeNavigate(R.id.action_nav_exposure_to_nav_spread_prevention, R.id.nav_exposures) }
        earlier_exposures_container.setOnClickListener { safeNavigate(R.id.action_nav_exposure_to_nav_recent_exposures, R.id.nav_exposures) }
    }

    private fun onNoRecentExposures() {
        no_exposures_header.text = AppConfig.noEncounterHeader
        no_exposures_body.text = AppConfig.noEncounterBody

        no_exposures_group.show()

        exposures_group.hide()
        container_group.hide()
    }

    private fun onRecentExposures() {
        exposures_group.show()
        container_group.show()

        no_exposures_group.hide()
    }

}
