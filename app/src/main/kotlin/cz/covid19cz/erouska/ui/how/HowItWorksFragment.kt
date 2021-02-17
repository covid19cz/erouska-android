package cz.covid19cz.erouska.ui.how

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentExposureBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.how.event.HowItWorksEvent
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HowItWorksFragment : BaseFragment<FragmentExposureBinding, HowItWorksVM>(
    R.layout.fragment_how_it_works,
    HowItWorksVM::class
) {

    companion object {
        private const val SCREEN_NAME = "How It Works"
    }

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.CLOSE)

        activity?.title = AppConfig.howItWorksUITitle

        subscribe(HowItWorksEvent::class) {
            when (it.command) {
                HowItWorksEvent.Command.WRITE_EMAIL -> onWriteEmail()
                HowItWorksEvent.Command.CLOSE -> onClose()
            }
        }
    }

    private fun onWriteEmail() {
        supportEmailGenerator.sendSupportEmail(
            requireActivity(),
            lifecycleScope,
            recipient = AppConfig.supportEmail,
            isError = false,
            screenOrigin = SCREEN_NAME
        )
    }

    private fun onClose() {
        navController().navigateUp()
    }
}