package cz.covid19cz.erouska.ui.help

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_help.*
import kotlinx.android.synthetic.main.search_toolbar.*
import javax.inject.Inject

@AndroidEntryPoint
class HelpFragment : BaseFragment<FragmentHelpBinding, HelpVM>(
    R.layout.fragment_help,
    HelpVM::class
) {

    companion object {
        private const val SCREEN_NAME = "Help"
    }

    private val args: HelpFragmentArgs by navArgs()

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fillInHelp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.fullscreen.let { fullscreen ->
            enableUpInToolbar(
                fullscreen,
                if (fullscreen) {
                    IconType.CLOSE
                } else {
                    IconType.UP
                }
            )
        }

        activity?.toolbar_search_view?.apply {

            setQuery("", false)

            setOnSearchClickListener {
                viewModel.onSearchTapped()
            }

            isIconified = true

        }

        support_button.setOnClickListener {
            supportEmailGenerator.sendSupportEmail(
                requireActivity(),
                lifecycleScope,
                isError = false,
                screenOrigin = SCREEN_NAME
            )
        }

    }

}
