package cz.covid19cz.erouska.ui.noverificationcode

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentVerificationBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.noverificationcode.event.NoVerificationCodeEvent
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_error.email_button
import kotlinx.android.synthetic.main.fragment_no_verification_code.*
import javax.inject.Inject

@AndroidEntryPoint
class NoVerificationCodeFragment : BaseFragment<FragmentVerificationBinding, NoVerificationCodeVM>(
    R.layout.fragment_no_verification_code,
    NoVerificationCodeVM::class
) {

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(true, IconType.CLOSE)

        subscribe(NoVerificationCodeEvent::class) {
            when (it.command) {
                NoVerificationCodeEvent.Command.WRITE_EMAIL -> onWriteEmail()
            }
        }

        no_verification_code_body.text = getString(R.string.no_verification_body, AppConfig.supportEmail)
        no_verification_code_caption.text = getString(R.string.no_verification_caption, AppConfig.supportEmail)
    }

    private fun onWriteEmail() {
        email_button.setOnClickListener {
            supportEmailGenerator.sendVerificationEmail(requireActivity())
        }
    }
}