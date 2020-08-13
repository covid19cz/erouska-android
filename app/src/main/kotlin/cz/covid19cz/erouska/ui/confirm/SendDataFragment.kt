package cz.covid19cz.erouska.ui.confirm

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSendDataBinding
import cz.covid19cz.erouska.ext.focusAndShowKeyboard
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.hideKeyboard
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.confirm.event.SendDataCommandEvent
import cz.covid19cz.erouska.ui.confirm.event.SendDataFailedState
import kotlinx.android.synthetic.main.fragment_send_data.*

class SendDataFragment : BaseFragment<FragmentSendDataBinding, SendDataVM>(
    R.layout.fragment_send_data,
    SendDataVM::class
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(SendDataCommandEvent::class) {
            when (it.command) {
                SendDataCommandEvent.Command.INIT -> onInitState()
                SendDataCommandEvent.Command.CODE_VALID -> onCodeValid()
                SendDataCommandEvent.Command.CODE_INVALID -> onCodeInvalid()
                SendDataCommandEvent.Command.CODE_EXPIRED -> onCodeExpired()
                SendDataCommandEvent.Command.DATA_SEND_FAILURE -> onSendDataFailure()
                SendDataCommandEvent.Command.DATA_SEND_SUCCESS -> onSendDataSuccess()
                SendDataCommandEvent.Command.PROCESSING -> onProcess()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.FLAVOR == "dev") {
            debug_buttons_container.show()
        }

        setupListeners()
    }

    private fun setupListeners() {
        confirm_button.setOnClickListener { viewModel.verifyAndConfirm(code_input.text?.toString()) }
        back_button.setOnClickListener { viewModel.reset() }
        try_again_button.setOnClickListener { viewModel.sendData() }
        close_button.setOnClickListener { navController().navigateUp() }
        success_close_button.setOnClickListener { navController().navigateUp() }
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
        code_input.text = null

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
        code_input_layout.error = getString(R.string.send_data_code_invalid)
    }

    private fun onCodeValid() {
        code_input_layout.error = null
    }

    private fun onCodeExpired() {
        progress.hide()
        code_input.hideKeyboard()

        ic_error.show()
        error_header.show()
        error_body.show()
        back_button.show()

        send_data_body.hide()
        code_input_layout.hide()
        confirm_button.hide()

        error_header.text = getString(R.string.send_data_code_expired_header)
        error_body.text = getString(R.string.send_data_code_expired_body)
    }

    private fun onSendDataFailure() {
        progress.hide()
        code_input.hideKeyboard()
        enableUpInToolbar(true, IconType.CLOSE)

        ic_error.show()
        error_header.show()
        error_body.show()
        try_again_button.show()
        close_button.show()

        send_data_body.hide()
        code_input_layout.hide()
        confirm_button.hide()

        error_header.text = getString(R.string.send_data_failure_header)
        error_body.text = getString(R.string.send_data_failure_body)

    }

    private fun onSendDataSuccess() {
        progress.hide()
        code_input.hideKeyboard()
        enableUpInToolbar(true, IconType.CLOSE)

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