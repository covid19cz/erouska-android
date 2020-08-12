package cz.covid19cz.erouska.ui.welcome

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentWelcomeBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.welcome.event.WelcomeCommandEvent
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment :
    BaseFragment<FragmentWelcomeBinding, WelcomeVM>(R.layout.fragment_welcome, WelcomeVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(WelcomeCommandEvent::class) {
            when (it.command) {
                WelcomeCommandEvent.Command.VERIFY_APP -> openAppActivation()
                WelcomeCommandEvent.Command.HELP -> openHelpPage()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)

        if (viewModel.wasAppUpdated()) {
            navigate(R.id.action_nav_welcome_fragment_to_nav_legacy_update)
        }

        val welcomeDescription: String = String.format(
            getString(R.string.welcome_description),
            viewModel.getProclamationUrl()
        )

        welcome_desc.text =
            HtmlCompat.fromHtml(welcomeDescription, HtmlCompat.FROM_HTML_MODE_LEGACY)
        welcome_desc.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun openAppActivation() {
        navigate(R.id.action_nav_welcome_fragment_to_nav_activation)
    }

    private fun openHelpPage() {
        navigate(
            R.id.action_nav_welcome_fragment_to_nav_help,
            Bundle().apply { putBoolean("fullscreen", true) })
    }
}