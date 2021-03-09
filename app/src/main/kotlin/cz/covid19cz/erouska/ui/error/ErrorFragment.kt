package cz.covid19cz.erouska.ui.error

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentVerificationBinding
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.error.entity.ErrorType
import cz.covid19cz.erouska.ui.verification.VerificationFragment
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_error.*
import javax.inject.Inject

@AndroidEntryPoint
class ErrorFragment : BaseFragment<FragmentVerificationBinding, ErrorVM>(
    R.layout.fragment_error,
    ErrorVM::class
) {

    companion object {
        private const val SCREEN_NAME = "Error Screen"
    }

    val args: ErrorFragmentArgs by navArgs()

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(true, IconType.CLOSE)

        when (args.type) {
            ErrorType.EXPIRED_CODE -> {
                onCodeExpiredOrUsed(args.errorCode)
            }
            ErrorType.INVALID_CODE -> {
                onCodeExpiredOrUsed(args.errorCode)
            }
            ErrorType.GENERAL_ERROR -> {
                onGeneralError(args.errorCode)
            }
            ErrorType.NO_INTERNET -> {
                onNoInternet()
            }
        }
    }

    private fun onCodeExpiredOrUsed(errorMessage: String) {
        email_button.show()
        close_button.hide()
        try_again_button.hide()

        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text = getString(R.string.send_data_code_expired_or_used, AppConfig.supportEmail)

        email_button.setOnClickListener {
            supportEmailGenerator.sendVerificationEmail(requireActivity())
        }
     }

    private fun onGeneralError(errorMessage: String) {
        email_button.show()
        close_button.hide()
        try_again_button.hide()

        error_desc.show()
        error_desc.text = getString(R.string.general_desc)

        email_button.text = getString(R.string.support_request_button)
        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text = getString(R.string.general_error, AppConfig.supportEmail, errorMessage)

        email_button.setOnClickListener {
            supportEmailGenerator.sendSupportEmail(
                requireActivity(),
                lifecycleScope,
                errorCode = errorMessage,
                isError = true,
                screenOrigin = VerificationFragment.SCREEN_NAME
            )
        }
    }

    private fun onNoInternet() {
        email_button.hide()
        close_button.show()
        try_again_button.show()

        close_button.setOnClickListener {
            navigate(ErrorFragmentDirections.actionNavErrorToNavDashboard())
        }
        try_again_button.setOnClickListener { navController().navigateUp() }

        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text = getString(R.string.no_internet_error)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigate(ErrorFragmentDirections.actionNavErrorToNavDashboard())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}