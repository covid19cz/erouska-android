package cz.covid19cz.erouska.ui.verification

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentVerificationBinding
import cz.covid19cz.erouska.ext.attachKeyboardController
import cz.covid19cz.erouska.ext.setOnDoneListener
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.main.MainActivityOld
import cz.covid19cz.erouska.ui.verification.event.VerificationCommandEvent
import cz.covid19cz.erouska.ui.verification.event.VerificationCommandEvent.Command
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationFragment : BaseFragment<FragmentVerificationBinding, VerificationVM>(
    R.layout.fragment_verification,
    VerificationVM::class
) {

    companion object {
        const val SCREEN_NAME = "Verification"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribe(VerificationCommandEvent::class) {
            when (it.command) {
                Command.NAV_SYMPTOMS -> navigate(R.id.action_nav_verification_to_nav_symptom_date)
                Command.NAV_NO_CODE -> navigate(R.id.action_nav_error_to_nav_no_verification_code)
            }
        }

        enableUpInToolbar(true, IconType.CLOSE)
        setupListeners()
        activity?.let {
            (it as MainActivityOld).initReviews()
        }
        with(binding) {
            codeInput.attachKeyboardController()
            codeInput.requestFocus()
            codeInput.setOnDoneListener { viewModel.verifyAndConfirm() }
        }
    }

    private fun setupListeners() {
        with(binding) {
            codeInput.attachKeyboardController()
            codeInput.setOnDoneListener { viewModel.verifyAndConfirm() }
        }
    }
}