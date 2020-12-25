package cz.covid19cz.erouska.ui.helpquestion

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpQuestionBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.Markdown
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_help_question.*
import javax.inject.Inject

@AndroidEntryPoint
class HelpQuestionFragment : BaseFragment<FragmentHelpQuestionBinding, HelpQuestionVM>(
    R.layout.fragment_help_question,
    HelpQuestionVM::class
) {

    @Inject
    internal lateinit var markdown: Markdown
    private var isFullscreen: Boolean = false

    private val args: HelpQuestionFragmentArgs by navArgs()

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isFullscreen = arguments?.let {
            HelpQuestionFragmentArgs.fromBundle(it).fullscreen
        } ?: false

    }

    fun goBack() {
        navController().navigateUp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)

        setTitle(args.question.question)
        question.text = args.question.question
        answer.text = args.question.answer

    }

}
