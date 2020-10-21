package cz.covid19cz.erouska.ui.spreadprevention

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSpreadPreventionBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpreadPreventionFragment : BaseFragment<FragmentSpreadPreventionBinding, SpreadPreventionVM>(
    R.layout.fragment_spread_prevention,
    SpreadPreventionVM::class
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true)

        activity?.title = AppConfig.spreadPreventionUITitle
    }

}