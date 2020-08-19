package cz.covid19cz.erouska.ui.exposure

import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSpreadPreventionBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.exposure.entity.PreventionData
import cz.covid19cz.erouska.ui.exposure.event.PreventionEvent
import kotlinx.android.synthetic.main.fragment_spread_prevention.*

class SpreadPreventionFragment : BaseFragment<FragmentSpreadPreventionBinding, SpreadPreventionVM>(
    R.layout.fragment_spread_prevention,
    SpreadPreventionVM::class
) {

    private val adapter: PreventionAdapter by lazy {
        PreventionAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.observe(this) { updateState(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = AppConfig.spreadPreventionUITitle
    }

    private fun updateState(state: PreventionEvent) {
        when (state) {
            is PreventionEvent.PreventionDataLoaded -> setData(state.data)
        }
    }

    private fun setData(data: PreventionData) {
        prevention_text.text = data.title
        prevention_list.adapter = adapter
        prevention_list.setHasFixedSize(true)
        adapter.setItems(data.items)
    }

}