package cz.covid19cz.erouska.ui.verification

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentVerificationBinding
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsErrorHandling
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.main.MainActivity
import cz.covid19cz.erouska.ui.verification.event.SendDataCommandEvent
import cz.covid19cz.erouska.ui.verification.event.SendDataFailedState
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import cz.covid19cz.erouska.utils.showOrHide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_verification.*
import javax.inject.Inject

@AndroidEntryPoint
class VerificationFragment : BaseFragment<FragmentVerificationBinding, VerificationVM>(
    R.layout.fragment_verification,
    VerificationVM::class
) {

    companion object {
        private const val SCREEN_NAME = "Verification"
    }

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        activity?.let {
            (it as MainActivity).initReviews()
        }
        binding.codeInput.attachKeyboardController()
        binding.codeInput.requestFocus()
        binding.codeInput.setOnDoneListener { viewModel.verifyAndConfirm() }
    }

    private fun setupListeners() {
        code_input.attachKeyboardController()
        code_input.setOnDoneListener { viewModel.verifyAndConfirm() }
    }

}