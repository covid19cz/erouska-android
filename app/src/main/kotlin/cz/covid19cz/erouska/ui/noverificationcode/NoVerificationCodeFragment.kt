package cz.covid19cz.erouska.ui.noverificationcode

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentNoVerificationCodeBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.noverificationcode.event.WriteEmailEvent
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NoVerificationCodeFragment : BaseFragment<FragmentNoVerificationCodeBinding, NoVerificationCodeVM>(
    R.layout.fragment_no_verification_code,
    NoVerificationCodeVM::class
) {

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(true, IconType.CLOSE)

        subscribe(WriteEmailEvent::class) {
            onWriteEmail()
        }

        with(binding) {
            noVerificationCodeBody.text = getString(R.string.no_verification_body, AppConfig.supportEmail)
            noVerificationCodeCaption.text = getString(R.string.no_verification_caption, AppConfig.supportEmail)
        }
    }

    private fun onWriteEmail() {
        binding.emailButton.setOnClickListener {
            supportEmailGenerator.sendVerificationEmail(requireActivity())
        }
    }
}