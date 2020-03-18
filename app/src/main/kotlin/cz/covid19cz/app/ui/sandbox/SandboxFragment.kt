package cz.covid19cz.app.ui.sandbox

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.snackbar.Snackbar
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentSandboxBinding
import cz.covid19cz.app.service.CovidService
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.sandbox.event.ServiceCommandEvent
import cz.covid19cz.app.utils.Log
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

        subscribe(ExportEvent.Complete::class) { event ->
             view?.let {
                    Snackbar.make(it, event.fileName, Snackbar.LENGTH_LONG).show()
            }
        }

        if (isMyServiceRunning(CovidService::class.java)) {
            Log.d("Service Covid is running")
            viewModel.serviceRunning.value = true
        } else {
            Log.d("Service Covid is not running")
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vLogin.setOnClickListener {
            navigate(R.id.action_nav_sandbox_to_nav_login)
        }

        setToolbarTitle(R.string.bluetooth_toolbar_title)
        enableUpInToolbar(false)
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