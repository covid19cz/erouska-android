package cz.covid19cz.erouska.ui.permissions

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import cz.covid19cz.erouska.R

class PermissionsDisabledFragment :
    BasePermissionsFragment<PermissionDisabledVM>(R.layout.fragment_permissionss_disabled, PermissionDisabledVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableUpInToolbar(false)
        viewModel.initViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}