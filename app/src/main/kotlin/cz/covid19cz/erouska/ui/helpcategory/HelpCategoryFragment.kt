package cz.covid19cz.erouska.ui.helpcategory

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpCategoryBinding
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.search_toolbar.*

@AndroidEntryPoint
class HelpCategoryFragment : BaseFragment<FragmentHelpCategoryBinding, HelpCategoryVM>(
    R.layout.fragment_help_category,
    HelpCategoryVM::class
) {

    private val args: HelpCategoryFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)
        activity?.title = args.category.title

        viewModel.fillInQuestions(args.category.questions)

        activity?.toolbar_search_view?.apply {

            setQuery("", false)

            setOnSearchClickListener {
                viewModel.onSearchTapped()
            }

            isIconified = true
            show()

        }

    }

}
