package cz.covid19cz.app.ui.help

import android.os.Bundle
import android.view.View
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentHelpBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.help.event.HelpCommandEvent

class HelpFragment : BaseFragment<FragmentHelpBinding, HelpVM>(R.layout.fragment_help, HelpVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(HelpCommandEvent::class) {
            when(it.command){
                HelpCommandEvent.Command.GO_BACK -> goBack()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true)
    }

    fun goBack(){
        navController().navigateUp()
    }
}