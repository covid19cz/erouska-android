package cz.covid19cz.erouska.ui.help

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.search_toolbar.*
import java.util.regex.Pattern
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
        showSearchView()

        // Associate searchable configuration with the SearchView
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        activity?.toolbar_search_view?.apply {
            isIconifiedByDefault = true

            setOnSearchClickListener {
                menu.findItem(R.id.previous_search_result)?.isVisible = true
                menu.findItem(R.id.next_search_result)?.isVisible = true
                menu.findItem(R.id.nav_about)?.isVisible = false
                (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)
            }

            setOnCloseListener {
                menu.findItem(R.id.previous_search_result)?.isVisible = false
                menu.findItem(R.id.next_search_result)?.isVisible = false
                menu.findItem(R.id.nav_about)?.isVisible = true
                (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(true)
                false
            }

            setSearchControlButtons(menu, false)

            setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        scrollToNextResult(query)
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
                            lastMarkedIndex = 0
                            setSearchControlButtons(menu, true)

                            val pattern = query.trim()
                            val r =
                                Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                            var result = AppConfig.helpMarkdown
                            val m = r.matcher(result)
                            val replaceList = arrayListOf<String>()
                            while (m.find()) {
                                replaceList.add(result.substring(m.start(0), m.end(0)))
                            }

                            showSnackBar("${replaceList.size} search results")

                            for (replaceString in replaceList.distinct()) {
                                result = result.replace(replaceString, "**${replaceString}**")
                            }

                            lastMarkedIndex =
                                help_desc.text.toString().indexOf(query, ignoreCase = true)
                            val lineNumber: Int =
                                help_desc.layout.getLineForOffset(lastMarkedIndex)

                            markdown.show(help_desc, result)

                            if (lineNumber >= 0) {
                                help_scroll.scrollTo(0, help_desc.layout.getLineTop(lineNumber))
                            }


                        } else {
                            markdown.show(help_desc, AppConfig.helpMarkdown)
//                            help_scroll.scrollTo(0, 0)

                            // show SnackBar only if user made some search query
                            if (query.isNotBlank()) {
                                showSnackBar("No search result")
                            }
                            setSearchControlButtons(menu, false)
                        }
                    }
                    return true
                }
            })
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        collapseAndHideSearchView()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
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
            R.id.previous_search_result -> {
                scrollToPreviousResult(activity?.toolbar_search_view?.query.toString())
                true
            }
            R.id.next_search_result -> {
                scrollToNextResult(activity?.toolbar_search_view?.query.toString())
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showSearchView() {
        activity?.toolbar_search_view?.show()
    }

    private fun collapseAndHideSearchView() {
        activity?.toolbar_search_view?.apply {
            setQuery("", false)
            isIconified = true
            hide()
        }
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    /**
     * Scroll to previous search result
     */
    private fun scrollToPreviousResult(query: String) {
        if (query.isNotBlank() && AppConfig.helpMarkdown.contains(query, ignoreCase = true)) {
            if (lastMarkedIndex == -1) {
                lastMarkedIndex = help_desc.text.toString().length
            }
            lastMarkedIndex = help_desc.text.toString().substring(0, lastMarkedIndex).lastIndexOf(
                query, ignoreCase = true
            )
            val lineNumber: Int = help_desc.layout.getLineForOffset(lastMarkedIndex)
            help_scroll.scrollTo(0, help_desc.layout.getLineTop(lineNumber))
        }
    }

    /**
     * Scroll to next search result
     */
    private fun scrollToNextResult(query: String) {
        if (query.isNotBlank() && AppConfig.helpMarkdown.contains(query, ignoreCase = true)) {
            lastMarkedIndex = help_desc.text.toString().indexOf(
                query,
                lastMarkedIndex + query.length,
                ignoreCase = true
            )
            val lineNumber: Int = help_desc.layout.getLineForOffset(lastMarkedIndex)
            help_scroll.scrollTo(0, help_desc.layout.getLineTop(lineNumber))
        }
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

    private fun setSearchControlButtons(menu: Menu, shouldEnable: Boolean) {
        menu.findItem(R.id.previous_search_result)?.icon?.alpha = if (shouldEnable) 255 else 75
        menu.findItem(R.id.next_search_result)?.icon?.alpha = if (shouldEnable) 255 else 75
        menu.findItem(R.id.previous_search_result)?.isEnabled = shouldEnable
        menu.findItem(R.id.next_search_result)?.isEnabled = shouldEnable
    }
}