package cz.covid19cz.app.ui.welcome

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentWelcomeBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.welcome.event.WelcomeCommandEvent
import cz.covid19cz.app.utils.Auth

class WelcomeFragment : BaseFragment<FragmentWelcomeBinding, WelcomeVM>(R.layout.fragment_welcome, WelcomeVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(WelcomeCommandEvent::class) {
            when (it.command) {
                WelcomeCommandEvent.Command.OPEN_BT_ONBOARD -> openBluetoothOnboard()
                WelcomeCommandEvent.Command.VERIFY_APP -> openAppVerification()
                WelcomeCommandEvent.Command.HELP -> openHelpPage()
            }
        }

        if (Auth.isSignedIn()) {
            navigate(R.id.action_nav_welcome_fragment_to_nav_sandbox, null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph,
                        true).build())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)
    }

    fun openBluetoothOnboard() {
        navigate(R.id.action_nav_welcome_fragment_to_nav_bt_onboard)
    }

    fun openAppVerification() {
        navigate(R.id.action_nav_welcome_fragment_to_nav_login)
    }

    fun openHelpPage() {
        navigate(R.id.action_nav_welcome_fragment_to_nav_help, Bundle().apply { putBoolean("fullscreen", true) })
    }
}