package cz.covid19cz.erouska.ui.help

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpBinding
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.search_toolbar.*

@AndroidEntryPoint
class HelpFragment : BaseFragment<FragmentHelpBinding, HelpVM>(
    R.layout.fragment_help,
    HelpVM::class
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fillInHelp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)

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
