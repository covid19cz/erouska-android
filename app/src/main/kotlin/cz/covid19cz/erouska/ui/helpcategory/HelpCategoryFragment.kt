package cz.covid19cz.erouska.ui.helpcategory

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpCategoryBinding
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.search_toolbar.*

@AndroidEntryPoint
class HelpCategoryFragment : BaseFragment<FragmentHelpCategoryBinding, HelpCategoryVM>(
    R.layout.fragment_help_category,
    HelpCategoryVM::class
) {

    private val args: HelpCategoryFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fillInQuestions(args.category.questions)
        viewModel.categoryTitle = args.category.title
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)
        activity?.title = viewModel.categoryTitle

        activity?.toolbar_search_view?.apply {

            setQuery("", false)

            setOnSearchClickListener {
                viewModel.onSearchTapped()
            }

            isIconified = true

        }

    }

}
