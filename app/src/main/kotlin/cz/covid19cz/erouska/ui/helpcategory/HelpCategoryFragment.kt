package cz.covid19cz.erouska.ui.helpcategory

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpCategoryBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.Markdown
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HelpCategoryFragment : BaseFragment<FragmentHelpCategoryBinding, HelpCategoryVM>(
    R.layout.fragment_help_category,
    HelpCategoryVM::class
) {

    @Inject
    internal lateinit var markdown: Markdown

    private val args: HelpCategoryFragmentArgs by navArgs()

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.help, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(true, IconType.CLOSE)

        setTitle(args.category.title)
        viewModel.fillInQuestions(args.category.questions)

    }

}
