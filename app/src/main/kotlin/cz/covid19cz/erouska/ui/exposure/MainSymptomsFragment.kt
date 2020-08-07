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
        activity?.title = AppConfig.symptomsUITitle

        main_symptoms_body.text = AppConfig.mainSymptomsBody
    }


}