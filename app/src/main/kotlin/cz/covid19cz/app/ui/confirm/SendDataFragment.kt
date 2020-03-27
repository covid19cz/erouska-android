package cz.covid19cz.app.ui.confirm

import cz.covid19cz.app.R

class SendDataFragment: ConfirmationFragment() {
    override val confirmDescription by lazy { getString(R.string.upload_confirmation) }
    override val confirmButtonTextRes = R.string.yes_send
    override val successShortText: String by lazy { getString(R.string.upload_data_success_text) }
    override val successDescription: String by lazy { getString(R.string.upload_data_success_description) }

    override fun doOnConfirm() {
        viewModel.sendData()
    }

    override fun doOnClose() {
        navController().navigateUp()
    }
}
