package cz.covid19cz.app.ui.confirm

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.covid19cz.app.R

class SendDataFragment: ConfirmationFragment() {
    override val description by lazy { getString(R.string.upload_confirmation) }
    override val buttonTextRes = R.string.yes_send

    override fun confirmedClicked() {
        viewModel.sendData()
    }

    override fun doWhenFinished() {
        MaterialAlertDialogBuilder(context)
            .setMessage(R.string.upload_successful)
            .setPositiveButton(getString(android.R.string.ok))
            { _, _ ->
                navController().navigateUp()
            }
            .show()
    }

}