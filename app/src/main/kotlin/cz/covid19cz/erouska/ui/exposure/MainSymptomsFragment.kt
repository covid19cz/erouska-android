package cz.covid19cz.erouska.ui.exposure

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentMainSymptomsBinding
import cz.covid19cz.erouska.ui.base.BaseFragment

class MainSymptomsFragment : BaseFragment<FragmentMainSymptomsBinding, MainSymptomsVM>(
    R.layout.fragment_main_symptoms,
    MainSymptomsVM::class
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = AppConfig.symptomsUITitle
    }


}