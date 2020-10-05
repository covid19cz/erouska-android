package cz.covid19cz.erouska.ui.help

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpBinding
import cz.covid19cz.erouska.ext.*
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

    var lastMarkedIndex = 0
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.help, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            isIconifiedByDefault = true
            setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        if (query.isNotBlank() && AppConfig.helpMarkdown.contains(query)) {
                            lastMarkedIndex = help_desc.text.toString().indexOf(
                                query,
                                lastMarkedIndex + query.length
                            )
                            val lineNumber: Int = help_desc.layout.getLineForOffset(lastMarkedIndex)

                            help_scroll.scrollTo(0, help_desc.layout.getLineTop(lineNumber))
                        }
                        return true
                    }
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    query?.let {
                        if (query.isNotBlank() && AppConfig.helpMarkdown.contains(
                                query,
                                ignoreCase = true
                            )
                        ) {
                            lastMarkedIndex =
                                AppConfig.helpMarkdown.indexOf(query, ignoreCase = true)
                            val lineNumber: Int = help_desc.layout.getLineForOffset(lastMarkedIndex)
                            val highlighted = "```${query.trim()}```"
                            var fullText = AppConfig.helpMarkdown.replace(
                                query.trim(),
                                highlighted,
                                ignoreCase = true
                            )
                            markdown.show(help_desc, fullText)

                            if (lineNumber >= 0) {
                                help_scroll.scrollTo(0, help_desc.layout.getLineTop(lineNumber))
                            }
                        } else {
                            markdown.show(help_desc, AppConfig.helpMarkdown)
                            help_scroll.scrollTo(0, 0)
                        }
                    }
                    return true
                }
            })
        }
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