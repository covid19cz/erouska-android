package cz.covid19cz.erouska.ui.exposure

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentRecentExposuresBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.exposure.event.RecentExposuresEvent
import kotlinx.android.synthetic.main.fragment_recent_exposures.*

class RecentExposuresFragment : BaseFragment<FragmentRecentExposuresBinding, RecentExposuresVM>(
    R.layout.fragment_recent_exposures,
    RecentExposuresVM::class
) {

    private val exposureAdapter: RecentExposuresAdapter by lazy {
        RecentExposuresAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = AppConfig.recentExposuresUITitle

        exposures_list.adapter = exposureAdapter
        exposures_list.setHasFixedSize(true)

        viewModel.state.observe(this, Observer { updateState(it) })
        viewModel.loadExposures()
    }

    private fun updateState(event: RecentExposuresEvent) {
        when (event) {
            is RecentExposuresEvent.ExposuresLoadedEvent -> exposureAdapter.updateItems(event.exposures)
        }
    }

}