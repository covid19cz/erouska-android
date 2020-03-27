package cz.covid19cz.app.ui.confirm

import cz.covid19cz.app.R
import cz.covid19cz.app.service.CovidService
import cz.covid19cz.app.utils.Auth

class DeleteUserFragment : ConfirmationFragment() {
    override val confirmDescription by lazy { getString(R.string.delete_user_desc, Auth.getPhoneNumber())}
    override val confirmButtonTextRes = R.string.delete_registration
    override val successShortText: String by lazy { getString(R.string.delete_user_success_text) }

    override fun doOnConfirm() {
        context?.let {
            it.startService(CovidService.stopService(it))
        }
        viewModel.deleteUser()
    }

    override fun doOnSuccess() {
        navController().popBackStack()
    }

    override fun doOnClose() {
        navController().navigate(R.id.action_deleteUserFragment_to_nav_welcome_fragment)
    }
}
