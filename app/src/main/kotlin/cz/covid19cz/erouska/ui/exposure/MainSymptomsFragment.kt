package cz.covid19cz.erouska.ui.exposure

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentMainSymptomsBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_main_symptoms.*

class MainSymptomsFragment : BaseFragment<FragmentMainSymptomsBinding, MainSymptomsVM>(
    R.layout.fragment_main_symptoms,
    MainSymptomsVM::class
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initText()
    }

    private fun initText() {
        main_symptoms_body.text = AppConfig.mainSymptomsBody
        temperature_label.text = AppConfig.symptomTemperature
        cough_label.text = AppConfig.symptomCough
        stuffiness_label.text = AppConfig.symptomStuffiness
        throat_ache_label.text = AppConfig.symptomThroatAche
        headache_label.text = AppConfig.symptomHeadAche
    }


}