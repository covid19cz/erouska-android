package cz.covid19cz.erouska.ui.exposureinfo

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentExposureInfoBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExposureInfoFragment : BaseFragment<FragmentExposureInfoBinding, ExposureInfoVM>(R.layout.fragment_exposure_info, ExposureInfoVM::class) {

    val args: ExposureInfoFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel.demo = args.demo
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.CLOSE)
        activity?.title = AppConfig.exposureUITitle
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.exposure, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}