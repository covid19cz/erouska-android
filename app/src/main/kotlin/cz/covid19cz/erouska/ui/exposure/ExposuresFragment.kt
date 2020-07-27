package cz.covid19cz.erouska.ui.exposure

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentExposuresBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_exposures.*

class ExposuresFragment : BaseFragment<FragmentExposuresBinding, ExposuresVM>(
    R.layout.fragment_exposures,
    ExposuresVM::class
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(true)

        // TODO Mock date, replace with date of last exposure
        val mockDate = "14. 5. 2020"
        last_exposure.text = String.format(AppConfig.exposureNotification, mockDate)
        exposure_help.text = AppConfig.exposureInfo

        setupListeners()
    }

    private fun setupListeners() {
        symptoms_container.setOnClickListener { }
        spread_prevention_container.setOnClickListener { }
        earlier_exposures_container.setOnClickListener { }
    }

}
