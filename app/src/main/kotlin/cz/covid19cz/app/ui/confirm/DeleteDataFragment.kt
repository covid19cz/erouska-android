package cz.covid19cz.app.ui.confirm

import cz.covid19cz.app.R
import cz.covid19cz.app.service.CovidService

class DeleteDataFragment : ConfirmationFragment() {
    override val descriptionRes = R.string.delete_data_description
    override val buttonTextRes = R.string.delete_data_button

    override fun confirmedClicked() {
        context?.let {
            it.startService(CovidService.stopService(it))
        }
        viewModel.deleteAllData()
    }

    override fun doWhenFinished() {
        navController().navigateUp()
    }
}