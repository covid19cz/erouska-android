package cz.covid19cz.erouska.ui.helpsearch

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpSearchBinding
import cz.covid19cz.erouska.ext.attachKeyboardController
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.L
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                goBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fillQuestions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)

        L.i("help search onViewCreated")

        // Associate searchable configuration with the SearchView
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        activity?.toolbar_search_view?.apply {

            attachKeyboardController()
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

            setOnCloseListener {
                goBack()
                true
            }

            show()
            requestFocus()

        }

    }

    override fun onBackPressed(): Boolean {
        L.i("On back pressed")
        goBack()
        return true
    }

    private fun removeSearchViewCallbacks() {
        activity?.toolbar_search_view?.apply {
            setOnCloseListener(null)
            setOnQueryTextListener(null)
        }
    }

    private fun goBack() {
        removeSearchViewCallbacks()
        collapseSearchView()
        navController().navigateUp()
    }

    private fun collapseSearchView() {
        activity?.toolbar_search_view?.apply {
            L.i("Collapsing")
            setQuery("", false)
        }
    }

}
