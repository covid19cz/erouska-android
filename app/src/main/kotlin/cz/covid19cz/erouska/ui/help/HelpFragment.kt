package cz.covid19cz.erouska.ui.help

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
import javax.inject.Inject

@AndroidEntryPoint
class HelpFragment : BaseFragment<FragmentHelpBinding, HelpVM>(
    R.layout.fragment_help,
    HelpVM::class
) {

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

            viewModel.searchControlsEnabled.observe(viewLifecycleOwner, Observer { enabled ->
                setSearchControlButtons(menu, enabled)
            })

            setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        viewModel.scrollToNextResult(query, help_desc?.text.toString())
                        return true
                    }
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    viewModel.searchQuery(query, help_desc?.text.toString())
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
                viewModel.scrollToPreviousResult(
                    activity?.toolbar_search_view?.query.toString(),
                    help_desc?.text.toString()
                )
                true
            }
            R.id.next_search_result -> {
                viewModel.scrollToNextResult(
                    activity?.toolbar_search_view?.query.toString(),
                    help_desc?.text.toString()
                )
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(isFullscreen, IconType.CLOSE)

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

        viewModel.lastMarkedIndex.observe(viewLifecycleOwner) {
            val lineNumber = help_desc?.layout?.getLineForOffset(it)
            val lineTop = help_desc.layout?.getLineTop(lineNumber ?: 0) ?: 0
            help_scroll?.scrollTo(0, lineTop)
        }

        viewModel.searchResultCount.observe(viewLifecycleOwner) {
            if (it == 0) {
                when {
                    viewModel.queryData.value.length >= 3 -> {
                        showSnackBarForever(R.string.help_no_results)
                    }
                    else -> {
                        hideSnackBar()
                    }
                }
            } else {
                hideSnackBar()
            }
        }

        viewModel.queryData.observe((viewLifecycleOwner)) {
            if (it.isBlank()) {
                hideSnackBar()
            }
        }

        viewModel.content.observe(viewLifecycleOwner) {
            if (help_desc != null) {
                markdown.show(help_desc, it)
            }
        }

    }

    private fun setSearchControlButtons(menu: Menu, shouldEnable: Boolean) {
        menu.findItem(R.id.previous_search_result)?.icon?.alpha = if (shouldEnable) 255 else 75
        menu.findItem(R.id.next_search_result)?.icon?.alpha = if (shouldEnable) 255 else 75
        menu.findItem(R.id.previous_search_result)?.isEnabled = shouldEnable
        menu.findItem(R.id.next_search_result)?.isEnabled = shouldEnable
    }
}