package cz.covid19cz.app.ui.sandbox

import android.Manifest
import android.Manifest.permission
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentSandboxBinding
import cz.covid19cz.app.service.CovidService
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.login.LoginActivity
import cz.covid19cz.app.ui.sandbox.event.ServiceCommandEvent
import kotlinx.android.synthetic.main.fragment_sandbox.vLogin

class SandboxFragment :
    BaseFragment<FragmentSandboxBinding, SandboxVM>(R.layout.fragment_sandbox, SandboxVM::class) {

    companion object {
        const val REQUEST_BT_ENABLE = 1000
        const val REQUEST_PERMISSION_FINE_LOCATION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(ServiceCommandEvent::class) {
            when (it.command) {
                ServiceCommandEvent.Command.TURN_ON -> tryStartBtService()
                ServiceCommandEvent.Command.TURN_OFF -> stopService()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vLogin.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
        }

        setToolbarTitle(R.string.bluetooth_toolbar_title)
        enableUpInToolbar(true)
    }



    override fun onBluetoothEnabled() {
        super.onBluetoothEnabled()
        tryStartBtService()
    }

    fun tryStartBtService() {
        if (viewModel.bluetoothRepository.hasBle(requireContext())) {
            if (!viewModel.bluetoothRepository.isBtEnabled()) {
                navigate(R.id.action_nav_sandbox_to_nav_bt_disabled)
                return
            }
            if (!hasLocationPermissions()) {
                requestLocationPermission()
            } else {
                startBtService()
            }
        } else {
            showSnackBar(R.string.error_ble_unsupported)
        }
    }

    fun stopService() {
        CovidService.stopService(requireContext())
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBtService()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun startBtService() {
        val power = viewModel.power.value - 1
        CovidService.startService(requireContext(), viewModel.deviceId.value, power)
        viewModel.confirmStart()
    }
}