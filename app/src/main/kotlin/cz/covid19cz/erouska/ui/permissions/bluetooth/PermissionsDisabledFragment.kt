package cz.covid19cz.erouska.ui.permissions.bluetooth

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ext.shareApp
import cz.covid19cz.erouska.ui.permissions.BasePermissionsFragment
import cz.covid19cz.erouska.ui.permissions.bluetooth.event.PermissionsEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionsDisabledFragment :
    BasePermissionsFragment<PermissionDisabledVM>(
        R.layout.fragment_permissionss_disabled,
        PermissionDisabledVM::class
    ) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableUpInToolbar(false)
        viewModel.initViewModel()

        subscribe(PermissionsEvent::class) {
            when (it.command) {
                PermissionsEvent.Command.ENABLE_BT -> requestEnableBt()
                PermissionsEvent.Command.ENABLE_LOCATION -> requestLocationEnable()
                PermissionsEvent.Command.ENABLE_BT_LOCATION -> requestLocationEnable()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_share -> {
                requireContext().shareApp()
                true
            }
            R.id.nav_about -> {
                navigate(R.id.nav_about)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}