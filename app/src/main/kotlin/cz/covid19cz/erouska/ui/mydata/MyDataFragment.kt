package cz.covid19cz.erouska.ui.mydata

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentMyDataBinding
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.Analytics
import cz.covid19cz.erouska.utils.Analytics.ANALYTICS_KEY_CURRENT_MEASURES
import cz.covid19cz.erouska.utils.CustomTabHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_data.*
import javax.inject.Inject

@AndroidEntryPoint
class MyDataFragment :
    BaseFragment<FragmentMyDataBinding, MyDataVM>(R.layout.fragment_my_data, MyDataVM::class) {

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AppConfig.updateNewsOnRequest) {
            refresh_container.isEnabled = false
        }

        measures_text.setOnClickListener {
            Analytics.logEvent(requireContext(), ANALYTICS_KEY_CURRENT_MEASURES)
            openMeasures()
        }
    }

    private fun openMeasures() {
        showWeb(viewModel.getMeasuresUrl(), customTabHelper)
    }
}
