package cz.covid19cz.erouska.ui.help

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentGuideBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent
import cz.covid19cz.erouska.utils.BatteryOptimization
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_guide.*

class GuideFragment : BaseFragment<FragmentGuideBinding, GuideVM>(R.layout.fragment_guide, GuideVM::class) {

    lateinit var markwon: Markwon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(HelpCommandEvent::class) {
            when (it.command) {
                HelpCommandEvent.Command.GO_BACK -> goBack()
            }
        }
        markwon = Markwon.builder(requireContext()).build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)

        BatteryOptimization.getTutorialMarkdown()?.let { markwon.setMarkdown(guide_desc, it) }
    }

    private fun goBack() {
        navController().navigateUp()
    }
}