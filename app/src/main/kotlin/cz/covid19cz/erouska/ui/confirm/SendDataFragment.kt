package cz.covid19cz.erouska.ui.confirm

import cz.covid19cz.erouska.R

class SendDataFragment : ConfirmationFragment() {
    override val description by lazy { getString(R.string.upload_confirmation) }
    override val buttonTextRes = R.string.yes_send
    override fun confirmedClicked() {
        viewModel.sendData()
    }

    override fun doWhenFinished() {
        navigate(R.id.action_nav_send_data_to_nav_send_data_success)
    }

}
