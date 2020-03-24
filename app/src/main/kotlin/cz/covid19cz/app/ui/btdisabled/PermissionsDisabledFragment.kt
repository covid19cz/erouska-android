package cz.covid19cz.app.ui.btdisabled

import android.os.Bundle
import cz.covid19cz.app.R
import cz.covid19cz.app.ui.onboarding.BasePermissionsFragment

class PermissionsDisabledFragment :
    BasePermissionsFragment<PermissionDisabledVM>(R.layout.fragment_permissionss_disabled, PermissionDisabledVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableUpInToolbar(false)
        viewModel.initViewModel()
    }
}