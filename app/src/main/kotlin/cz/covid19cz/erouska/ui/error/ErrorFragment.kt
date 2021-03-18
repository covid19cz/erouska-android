package cz.covid19cz.erouska.ui.error

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentErrorBinding
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.error.entity.ErrorType
import cz.covid19cz.erouska.ui.verification.VerificationFragment
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ErrorFragment : BaseFragment<FragmentErrorBinding, ErrorVM>(
    R.layout.fragment_error,
    ErrorVM::class
) {

    private val args: ErrorFragmentArgs by navArgs()

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(true, IconType.CLOSE)

        when (args.type) {
            ErrorType.EXPIRED_OR_USED_CODE -> {
                onCodeExpiredOrUsed()
            }
            ErrorType.INVALID_CODE -> {
                onCodeInvalid()
            }
            ErrorType.GENERAL_ERROR -> {
                onGeneralError(args.errorCode)
            }
            ErrorType.NO_INTERNET -> {
                onNoInternet()
            }
        }
    }

    private fun onCodeExpiredOrUsed() {
        with(binding) {
            emailButton.show()
            closeButton.hide()
            tryAgainButton.hide()

            errorHeader.text = getString(R.string.send_data_failure_header)
            errorBody.text =
                getString(R.string.send_data_code_expired_or_used, AppConfig.supportEmail)

            emailButton.setOnClickListener {
                supportEmailGenerator.sendVerificationEmail(requireActivity())
            }
        }
    }

    private fun onCodeInvalid() {
        with(binding){
            tryAgainButton.show()
            emailButton.show()
            closeButton.hide()

            emailButton.text = getString(R.string.support_request_button)

            errorHeader.text = getString(R.string.send_data_code_invalid_header)
            errorBody.text = getString(R.string.send_data_code_invalid_body, AppConfig.supportEmail)

            tryAgainButton.setOnClickListener { navController().navigateUp() }
            emailButton.setOnClickListener {
                supportEmailGenerator.sendVerificationEmail(requireActivity())
            }
        }
    }


    private fun onGeneralError(errorMessage: String) {
        with(binding) {
            emailButton.show()
            closeButton.hide()
            tryAgainButton.hide()

            errorDesc.show()
            errorDesc.text = getString(R.string.general_desc)

            emailButton.text = getString(R.string.support_request_button)
            errorHeader.text = getString(R.string.send_data_failure_header)
            errorBody.text = getString(R.string.general_error, AppConfig.supportEmail, errorMessage)

            emailButton.setOnClickListener {
                supportEmailGenerator.sendSupportEmail(
                    requireActivity(),
                    lifecycleScope,
                    errorCode = errorMessage,
                    isError = true,
                    screenOrigin = VerificationFragment.SCREEN_NAME
                )
            }
        }
    }

    private fun onNoInternet() {
        with(binding) {
            emailButton.hide()
            closeButton.show()
            tryAgainButton.show()

            tryAgainButton.setOnClickListener { navController().navigateUp() }
            closeButton.setOnClickListener {
                navigate(ErrorFragmentDirections.actionNavErrorToNavDashboard())
            }

            errorHeader.text = getString(R.string.send_data_failure_header)
            errorBody.text = getString(R.string.no_internet_error)
        }
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