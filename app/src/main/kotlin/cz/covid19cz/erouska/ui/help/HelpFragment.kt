package cz.covid19cz.erouska.ui.help

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpBinding
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.Markdown
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_help.*
import javax.inject.Inject

@AndroidEntryPoint
class HelpFragment :
    BaseFragment<FragmentHelpBinding, HelpVM>(R.layout.fragment_help, HelpVM::class) {

    @Inject
    internal lateinit var markdown: Markdown
    private var isFullscreen: Boolean = false

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(HelpCommandEvent::class) {
            when (it.command) {
                HelpCommandEvent.Command.GO_BACK -> goBack()
                HelpCommandEvent.Command.OPEN_CHATBOT -> openChatBot()
            }
        }

        isFullscreen = arguments?.let {
            HelpFragmentArgs.fromBundle(it).fullscreen
        } ?: false
    }

    private fun openChatBot() {
        showWeb(AppConfig.chatBotLink, customTabHelper)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.help, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(isFullscreen, IconType.CLOSE)
        markdown.show(help_desc, AppConfig.helpMarkdown)

        if (isFullscreen) {
            welcome_continue_btn.visibility = View.VISIBLE
        } else {
            welcome_continue_btn.visibility = View.GONE
        }

        if (AppConfig.showChatBotLink) {
            chat_group.show()
        } else {
            chat_group.hide()
        }
    }

    fun goBack() {
        navController().navigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_about -> {
                navigate(HelpFragmentDirections.actionNavHelpToNavAbout(isFullscreen))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}