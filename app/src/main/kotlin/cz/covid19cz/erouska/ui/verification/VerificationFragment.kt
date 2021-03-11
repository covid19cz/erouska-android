package cz.covid19cz.erouska.ui.verification

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentVerificationBinding
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.main.MainActivity
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VerificationFragment : BaseFragment<FragmentVerificationBinding, VerificationVM>(
    R.layout.fragment_verification,
    VerificationVM::class
) {

    companion object {
        const val SCREEN_NAME = "Verification"
    }

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(true, IconType.CLOSE)
        setupListeners()
        activity?.let {
            (it as MainActivity).initReviews()
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