package cz.covid19cz.app.ui.permissions

import android.os.Bundle
import cz.covid19cz.app.R

class PermissionsDisabledFragment :
    BasePermissionsFragment<PermissionDisabledVM>(R.layout.fragment_permissionss_disabled, PermissionDisabledVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableUpInToolbar(false)
        viewModel.initViewModel()
    }
}