package cz.covid19cz.erouska.ui.help

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentGuideBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent
import cz.covid19cz.erouska.utils.BatteryOptimization
import cz.covid19cz.erouska.utils.Markdown
import kotlinx.android.synthetic.main.fragment_guide.*
import org.koin.android.ext.android.inject

class GuideFragment :
    BaseFragment<FragmentGuideBinding, GuideVM>(R.layout.fragment_guide, GuideVM::class) {

    private val markdown by inject<Markdown>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(HelpCommandEvent::class) {
            when (it.command) {
                HelpCommandEvent.Command.GO_BACK -> goBack()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)
        markdown.show(guide_desc, BatteryOptimization.getTutorialMarkdown())
    }

    private fun goBack() {
        navController().navigateUp()
    }
}