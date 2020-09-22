package cz.covid19cz.erouska.ui.senddata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSendDataBinding
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.DashboardFragment
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.sandbox.SandboxFragment
import cz.covid19cz.erouska.ui.senddata.event.SendDataCommandEvent
import cz.covid19cz.erouska.ui.senddata.event.SendDataFailedState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_send_data.*

@AndroidEntryPoint
class SendDataFragment : BaseFragment<FragmentSendDataBinding, SendDataVM>(
    R.layout.fragment_send_data,
    SendDataVM::class
) {

    companion object {
        const val REQUEST_GMS_ERROR_RESOLUTION = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(GmsApiErrorEvent::class) {
            try {
                startIntentSenderForResult(
                    it.status.resolution?.intentSender,
                    REQUEST_GMS_ERROR_RESOLUTION,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (t : Throwable){
                it.status.resolveUnknownGmsError(requireContext())
            }
        }

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
        setupListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SandboxFragment.REQUEST_GMS_ERROR_RESOLUTION) {
            when(resultCode){
                Activity.RESULT_OK -> viewModel.verifyAndConfirm()
                Activity.RESULT_CANCELED -> viewModel.reset()
            }
        }
    }

    private fun setupListeners() {
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