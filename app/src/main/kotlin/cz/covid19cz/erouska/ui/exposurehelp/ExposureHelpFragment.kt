package cz.covid19cz.erouska.ui.exposurehelp

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentExposureHelpBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.exposurehelp.entity.ExposureHelpType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExposureHelpFragment : BaseFragment<FragmentExposureHelpBinding, ExposureHelpVM>(
    R.layout.fragment_exposure_help,
    ExposureHelpVM::class
) {

    val args: ExposureHelpFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableUpInToolbar(true, IconType.UP)

        when (args.type) {
            ExposureHelpType.SYMPTOMS -> {
                activity?.title = AppConfig.symptomsUITitle
                viewModel.setData(AppConfig.symptomsContentJson)
            }
            ExposureHelpType.PREVENTION -> {
                activity?.title = AppConfig.spreadPreventionUITitle
                viewModel.setData(AppConfig.preventionContentJson, bottomTitle = true)
            }
            ExposureHelpType.EXPOSURE -> {
                activity?.title = AppConfig.exposureHelpUITitle
                viewModel.setData(AppConfig.exposureHelpContentJson)
            }
        }
    }
}