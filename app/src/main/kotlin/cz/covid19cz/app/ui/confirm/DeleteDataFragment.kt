package cz.covid19cz.app.ui.confirm

import cz.covid19cz.app.R
import cz.covid19cz.app.service.CovidService

class DeleteDataFragment : ConfirmationFragment() {
    override val confirmDescription by lazy { getString(R.string.delete_data_description) }
    override val confirmButtonTextRes = R.string.delete_data_button
    override val successShortText: String by lazy { getString(R.string.delete_data_success_text) }

    override fun doOnConfirm() {
        context?.let {
            it.startService(CovidService.stopService(it))
        }
        viewModel.deleteAllData()
    }

    override fun doOnClose() {
        navController().navigateUp()
    }
}
