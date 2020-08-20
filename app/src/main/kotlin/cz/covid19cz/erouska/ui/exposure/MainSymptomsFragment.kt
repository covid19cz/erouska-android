package cz.covid19cz.erouska.ui.exposure

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import androidx.lifecycle.observe
import cz.covid19cz.erouska.databinding.FragmentMainSymptomsBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.exposure.entity.SymptomsData
import cz.covid19cz.erouska.ui.exposure.event.SymptomsEvent
import kotlinx.android.synthetic.main.fragment_main_symptoms.*

class MainSymptomsFragment : BaseFragment<FragmentMainSymptomsBinding, MainSymptomsVM>(
    R.layout.fragment_main_symptoms,
    MainSymptomsVM::class
) {

    private val symptomsAdapter: SymptomsAdapter by lazy {
        SymptomsAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.observe(this) { updateState(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = AppConfig.symptomsUITitle
    }

    private fun updateState(event: SymptomsEvent) {
        when (event) {
            is SymptomsEvent.SymptomsDataLoaded -> updateData(event.data)
        }
    }

    private fun updateData(data: SymptomsData) {
        main_symptoms_body.text = data.title
        symptoms_list.adapter = symptomsAdapter
        symptoms_list.setHasFixedSize(true)
        symptomsAdapter.updateData(data.items)
    }


}