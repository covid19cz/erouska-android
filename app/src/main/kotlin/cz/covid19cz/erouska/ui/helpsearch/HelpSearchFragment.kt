package cz.covid19cz.erouska.ui.helpsearch

import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.SearchView
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpSearchBinding
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.Markdown
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.search_toolbar.*
import javax.inject.Inject

@AndroidEntryPoint
class HelpSearchFragment : BaseFragment<FragmentHelpSearchBinding, HelpSearchVM>(
    R.layout.fragment_help_search,
    HelpSearchVM::class
) {

    @Inject
    internal lateinit var markdown: Markdown

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fillQuestions()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Associate searchable configuration with the SearchView
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        showSearchView()

        activity?.toolbar_search_view?.apply {

            setOnCloseListener {
                collapseAndHideSearchView()
                true
            }

            setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    viewModel.searchQuery(query)
                    return true
                }
            })

        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        collapseAndHideSearchView()
        activity?.toolbar_search_view?.hide()
    }

    private fun showSearchView() {
        activity?.toolbar_search_view?.show()
    }

    private fun collapseAndHideSearchView() {
        activity?.toolbar_search_view?.apply {
            setQuery("", false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)

        activity?.toolbar_search_view?.let {
            it.queryHint = "Napiste, co hledate"
            it.requestFocusFromTouch()
            it.setQuery("", false)
        }

    }

}
