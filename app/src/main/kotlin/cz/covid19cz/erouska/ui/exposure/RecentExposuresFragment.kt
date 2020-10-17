package cz.covid19cz.erouska.ui.exposure

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentRecentExposuresBinding
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_recent_exposures.*

@AndroidEntryPoint
class RecentExposuresFragment : BaseFragment<FragmentRecentExposuresBinding, RecentExposuresVM>(
    R.layout.fragment_recent_exposures,
    RecentExposuresVM::class
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true)
        activity?.title = AppConfig.recentExposuresUITitle
        viewModel.loadExposures()
    }
}