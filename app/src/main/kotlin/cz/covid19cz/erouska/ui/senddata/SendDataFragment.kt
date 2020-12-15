package cz.covid19cz.erouska.ui.senddata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSendDataBinding
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsErrorHandling
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.main.MainActivity
import cz.covid19cz.erouska.ui.senddata.event.SendDataCommandEvent
import cz.covid19cz.erouska.ui.senddata.event.SendDataFailedState
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import cz.covid19cz.erouska.utils.showOrHide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_send_data.*
import javax.inject.Inject

@AndroidEntryPoint
class SendDataFragment : BaseFragment<FragmentSendDataBinding, SendDataVM>(
    R.layout.fragment_send_data,
    SendDataVM::class
) {

    companion object {
        private const val SCREEN_NAME = "Send data"
    }

    private var errorMessage: String? = null

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    @Inject
    internal lateinit var exposureNotificationsErrorHandling: ExposureNotificationsErrorHandling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(GmsApiErrorEvent::class) {
            exposureNotificationsErrorHandling.handle(it, this, SCREEN_NAME)
        }

        subscribe(SendDataCommandEvent::class) {
            when (it.command) {
                SendDataCommandEvent.Command.INIT -> onInitState()
                SendDataCommandEvent.Command.CODE_VALID -> onCodeValid()
                SendDataCommandEvent.Command.CODE_INVALID -> onCodeInvalid()
                SendDataCommandEvent.Command.CODE_EXPIRED -> onCodeExpired()
                SendDataCommandEvent.Command.DATA_SEND_FAILURE -> onSendDataFailure(it.errorMessage)
                SendDataCommandEvent.Command.DATA_SEND_SUCCESS -> onSuccess()
                SendDataCommandEvent.Command.NOT_ENOUGH_KEYS -> onSuccess(hasEnoughKeys = false)
                SendDataCommandEvent.Command.PROCESSING -> onProcess()
                SendDataCommandEvent.Command.CODE_EXPIRED_OR_USED -> onCodeExpiredOrUsed()
                SendDataCommandEvent.Command.NO_INTERNET -> onNoInternet()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        activity?.let {
            (it as MainActivity).initReviews()
        }
        code_input.requestFocus()
        code_input.setOnDoneListener { viewModel.verifyAndConfirm() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ExposureNotificationsErrorHandling.REQUEST_GMS_ERROR_RESOLUTION) {
            when (resultCode) {
                Activity.RESULT_OK -> viewModel.verifyAndConfirm()
                Activity.RESULT_CANCELED -> viewModel.reset()
            }
        }
    }

    private fun setupListeners() {
        code_input.attachKeyboardController()
        success_close_button.setOnClickListener {
            navController().navigateUp()
            activity?.let {
                (it as MainActivity).askForReview()
            }
        }
        support_button.setOnClickListener {
            supportEmailGenerator.sendSupportEmail(
                requireActivity(),
                lifecycleScope,
                errorCode = this.errorMessage,
                isError = true,
                screenOrigin = SCREEN_NAME
            )
        }
    }

    private fun onProcess() {
        progress.show()

        send_data_group.hide()
        error_group.hide()
        success_group.hide()
    }

    private fun onInitState() {
        progress.hide()
        enableUpInToolbar(true, IconType.CLOSE)

        code_input_layout.error = null
        send_data_group.show()
        error_group.hide()
    }

    private fun onCodeInvalid() {
        onInitState()
        code_input_layout.error = getString(R.string.send_data_code_invalid)
    }

    private fun onCodeValid() {
        code_input_layout.error = null
    }

    private fun onError(showSupportButton: Boolean = false) {
        progress.hide()
        code_input.hideKeyboard()
        error_group.show()
        send_data_group.hide()
        support_button.showOrHide(showSupportButton)
        back_button.showOrHide(!showSupportButton)
    }

    private fun onCodeExpired() {
        onError()
        error_header.text = getString(R.string.send_data_code_expired_header)
        error_body.text = getString(R.string.send_data_code_expired_body)
    }

    private fun onCodeExpiredOrUsed() {
        onError()
        support_button.hide()
        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text = getString(R.string.send_data_code_expired_body)
    }

    private fun onSendDataFailure(errorMessage: String?) {
        this.errorMessage = errorMessage
        onError(showSupportButton = true)
        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text =
            getString(R.string.send_data_failure_body, AppConfig.supportEmail, errorMessage)
    }

    private fun onNoInternet() {
        onError()
        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text = getString(R.string.no_internet)
    }

    private fun onSuccess(hasEnoughKeys: Boolean = true) {
        activity?.setTitle(R.string.sent)
        progress.hide()
        code_input.clearFocus()
        enableUpInToolbar(true, IconType.UP)

        if (hasEnoughKeys) {
            success_header.setText(R.string.send_data_success_header)
            success_body_1.setText(R.string.send_data_success_body_1)
        } else {
            success_header.setText(R.string.send_data_success_body_1)
            success_body_1.setText(R.string.send_data_success_body_1_not_enough_keys)
        }

        success_group.show()
        send_data_group.hide()
        error_group.hide()
    }

    override fun onBackPressed(): Boolean {
        if (viewModel.state.value == SendDataFailedState) {
            viewModel.reset()
            return true
        }
        code_input.clearFocus()
        return super.onBackPressed()
    }

}