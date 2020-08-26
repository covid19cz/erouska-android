package cz.covid19cz.erouska.ui.mydata

import android.os.Bundle
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentMyDataBinding
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.mydata.event.MyDataCommandEvent
import cz.covid19cz.erouska.utils.CustomTabHelper
import org.koin.android.ext.android.inject

class MyDataFragment :
    BaseFragment<FragmentMyDataBinding, MyDataVM>(R.layout.fragment_my_data, MyDataVM::class) {

    private val customTabHelper by inject<CustomTabHelper>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(MyDataCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                MyDataCommandEvent.Command.MEASURES -> openMeasures()
            }
        }
    }

    private fun openMeasures() {
        showWeb(viewModel.getMeasuresUrl(), customTabHelper)
    }
}
