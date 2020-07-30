package cz.covid19cz.erouska.ui.exposure

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSpreadPreventionBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_spread_prevention.*

class SpreadPreventionFragment : BaseFragment<FragmentSpreadPreventionBinding, SpreadPreventionVM>(
    R.layout.fragment_spread_prevention,
    SpreadPreventionVM::class
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prevention_text.text = AppConfig.preventionBody
        clean_hands_label.text = AppConfig.preventionCleanHandsLabel
        disinfection_label.text = AppConfig.preventionDisinfectionLabel
        cough_label.text = AppConfig.preventionCoughLabel
        tissue_label.text = AppConfig.preventionTissuesLabel
        social_distance_label.text = AppConfig.preventionSocialDistancingLabel
        no_contact_label.text = AppConfig.preventionNoContactLabel
    }

}