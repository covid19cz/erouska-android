package cz.covid19cz.erouska.ui.help

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.text.HtmlCompat
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent
import kotlinx.android.synthetic.main.fragment_help.*

class HelpFragment : BaseFragment<FragmentHelpBinding, HelpVM>(R.layout.fragment_help, HelpVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(HelpCommandEvent::class) {
            when (it.command) {
                HelpCommandEvent.Command.GO_BACK -> goBack()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.help, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(arguments?.getBoolean("fullscreen") == true, IconType.CLOSE)

        if(arguments?.getBoolean("fullscreen") == true){
            welcome_continue_btn.visibility = View.VISIBLE
        } else {
            welcome_continue_btn.visibility = View.GONE
        }

        val helpDescription: String = String.format(
            getString(R.string.help_desc),
            viewModel.getTutorialUrl()
        )

        help_desc.text = HtmlCompat.fromHtml(helpDescription, HtmlCompat.FROM_HTML_MODE_LEGACY)
        help_desc.movementMethod = LinkMovementMethod.getInstance()
    }

    fun goBack() {
        navController().navigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_about -> {
                navigate(R.id.nav_about, Bundle().apply {
                    // replicate the
                    putBoolean("fullscreen", arguments?.getBoolean("fullscreen") ?: false)
                })
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}