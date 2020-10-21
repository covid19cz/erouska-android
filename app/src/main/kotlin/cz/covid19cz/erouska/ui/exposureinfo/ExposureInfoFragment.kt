package cz.covid19cz.erouska.ui.exposureinfo

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentExposureInfoBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExposureInfoFragment : BaseFragment<FragmentExposureInfoBinding, ExposureInfoVM>(R.layout.fragment_exposure_info, ExposureInfoVM::class) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.CLOSE)
        activity?.title = AppConfig.exposureUITitle
    }
}