package cz.covid19cz.app.ui.confirm

import cz.covid19cz.app.R
import cz.covid19cz.app.service.CovidService

class DeleteDataFragment : ConfirmationFragment() {
    override val description by lazy { getString(R.string.delete_data_description) }
    override val buttonTextRes = R.string.delete_data_button

    override fun confirmedClicked() {
        viewModel.deleteAllData()
    }

    override fun doWhenFinished() {
        context?.let {
            it.startService(CovidService.stopService(it))
        }
        navigate(R.id.action_nav_delete_data_to_nav_delete_data_success)
    }
}
