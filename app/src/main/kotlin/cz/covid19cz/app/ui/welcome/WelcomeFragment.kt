package cz.covid19cz.app.ui.welcome

import android.os.Bundle
import android.view.View
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentWelcomeBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.welcome.event.WelcomeCommandEvent

class WelcomeFragment : BaseFragment<FragmentWelcomeBinding, WelcomeVM>(R.layout.fragment_welcome, WelcomeVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(WelcomeCommandEvent::class) {
            when(it.command){
                WelcomeCommandEvent.Command.VERIFY_APP -> openAppVerification()
                WelcomeCommandEvent.Command.HELP -> openHelpPage()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.welcome_toolbar_title)
        enableUpInToolbar(false)
    }

    fun openAppVerification(){
        navigate(R.id.action_nav_welcome_fragment_to_nav_sandbox)
    }
    fun openHelpPage(){
        navigate(R.id.action_nav_welcome_fragment_to_nav_help)
    }
}