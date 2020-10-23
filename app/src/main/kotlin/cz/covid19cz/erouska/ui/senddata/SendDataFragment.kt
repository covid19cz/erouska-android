package cz.covid19cz.erouska.ui.senddata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSendDataBinding
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsErrorHandling
import cz.covid19cz.erouska.ext.focusAndShowKeyboard
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.hideKeyboard
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.main.MainActivity
import cz.covid19cz.erouska.ui.senddata.event.SendDataCommandEvent
import cz.covid19cz.erouska.ui.senddata.event.SendDataFailedState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_send_data.*

@AndroidEntryPoint
class SendDataFragment : BaseFragment<FragmentSendDataBinding, SendDataVM>(
    R.layout.fragment_send_data,
    SendDataVM::class
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(GmsApiErrorEvent::class) {
            ExposureNotificationsErrorHandling.handle(it, this)
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ExposureNotificationsErrorHandling.REQUEST_GMS_ERROR_RESOLUTION) {
            when(resultCode){
                Activity.RESULT_OK -> viewModel.verifyAndConfirm()
                Activity.RESULT_CANCELED -> viewModel.reset()
            }
        }
    }

    private fun setupListeners() {
        close_button.setOnClickListener { navController().navigateUp() }
        success_close_button.setOnClickListener {
            navController().navigateUp()
            activity?.let {
                (it as MainActivity).askForReview()
            }
        }
    }

    private fun onProcess() {
        progress.show()

        send_data_body.hide()
        code_input_layout.hide()
        confirm_button.hide()

        ic_error.hide()
        error_header.hide()
        error_body.hide()
        back_button.hide()
        try_again_button.hide()
        close_button.hide()

        ic_success.hide()
        success_header.hide()
        success_body_1.hide()
        success_body_2.hide()
        success_body_3.hide()
        success_close_button.hide()
    }

    private fun onInitState() {
        progress.hide()
        code_input.focusAndShowKeyboard()
        enableUpInToolbar(true, IconType.UP)

        code_input_layout.error = null

        send_data_body.show()
        code_input_layout.show()
        confirm_button.show()

        ic_error.hide()
        error_header.hide()
        error_body.hide()
        back_button.hide()
        try_again_button.hide()
        close_button.hide()
    }

    private fun onCodeInvalid() {
        onInitState()
        code_input_layout.error = getString(R.string.send_data_code_invalid)
    }

    private fun onCodeValid() {
        code_input_layout.error = null
    }

    private fun onError() {
        progress.hide()
        code_input.hideKeyboard()

        ic_error.show()
        error_header.show()
        error_body.show()
        back_button.show()

        send_data_body.hide()
        code_input_layout.hide()
        confirm_button.hide()
    }

    private fun onCodeExpired() {
        onError()
        error_header.text = getString(R.string.send_data_code_expired_header)
        error_body.text = getString(R.string.send_data_code_expired_body)
    }

    private fun onCodeExpiredOrUsed() {
        onError()
        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text = getString(R.string.send_data_code_expired_body)
    }

    private fun onSendDataFailure(errorMessage: String?) {
        onError()
        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text = getString(R.string.send_data_failure_body, errorMessage)
    }

    private fun onNoInternet() {
        onError()
        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text = getString(R.string.no_internet)
    }

    private fun onSuccess(hasEnoughKeys : Boolean = true) {
        activity?.setTitle(R.string.sent)
        progress.hide()
        code_input.hideKeyboard()
        enableUpInToolbar(true, IconType.CLOSE)

        if (hasEnoughKeys){
            success_header.setText(R.string.send_data_success_header)
            success_body_1.setText(R.string.send_data_success_body_1)
        } else {
            success_header.setText(R.string.send_data_success_body_1)
            success_body_1.setText(R.string.send_data_success_body_1_not_enough_keys)
        }

        ic_success.show()
        success_header.show()
        success_body_1.show()
        success_body_2.show()
        success_body_3.show()
        success_close_button.show()

        send_data_body.hide()
        code_input_layout.hide()
        confirm_button.hide()

        ic_error.hide()
        error_header.hide()
        error_body.hide()
        back_button.hide()
        try_again_button.hide()
        close_button.hide()
    }

    override fun onBackPressed(): Boolean {
        if (viewModel.state.value == SendDataFailedState) {
            viewModel.reset()
            return true
        }
        return super.onBackPressed()
    }

}