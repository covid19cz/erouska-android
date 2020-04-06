package cz.covid19cz.erouska.ui.confirm

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.service.CovidService
import cz.covid19cz.erouska.utils.Auth
import cz.covid19cz.erouska.utils.formatPhoneNumber

class DeleteUserFragment : ConfirmationFragment() {
    override val description by lazy { getString(R.string.delete_user_desc, Auth.getPhoneNumber().formatPhoneNumber())}
    override val buttonTextRes = R.string.delete_registration
    override fun confirmedClicked() {
        viewModel.deleteUser()
    }

    override fun doWhenFinished() {
        context?.let {
            it.startService(CovidService.stopService(
                context = it,
                hideNotification = true,
                clearScanningData = true)
            )
        }
        navigate(R.id.action_nav_delete_user_to_nav_delete_user_success)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.CLOSE)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.onboarding, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}
