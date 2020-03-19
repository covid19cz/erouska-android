package cz.covid19cz.app.ui.sandbox
import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentSandboxBinding
import cz.covid19cz.app.service.CovidService
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.sandbox.event.ServiceCommandEvent
import cz.covid19cz.app.utils.Log
import kotlinx.android.synthetic.main.fragment_sandbox.vLogin
import io.reactivex.disposables.CompositeDisposable

class SandboxFragment :
    BaseFragment<FragmentSandboxBinding, SandboxVM>(R.layout.fragment_sandbox, SandboxVM::class) {

    companion object {
        const val REQUEST_BT_ENABLE = 1000
        const val REQUEST_PERMISSION_FINE_LOCATION = 1001
    }

    lateinit var rxPermissions: RxPermissions
    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermissions = RxPermissions(this)

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
            compositeDisposable.add(rxPermissions
                .request(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                .subscribe { granted: Boolean ->
                    if (granted) {
                        startBtService()
                    } else {
                        //TODO: better dialog and navigate to settings
                        Toast.makeText(context, "Povolte přístup k poloze", Toast.LENGTH_LONG)
                            .show()
                    }
                })
        } else {
            showSnackBar(R.string.error_ble_unsupported)
        }
    }

    fun stopService() {
        CovidService.stopService(requireContext())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_BT_ENABLE -> {
                tryStartBtService()
            }
        }
    }

    fun startBtService() {
        val power = viewModel.power.value - 1
        CovidService.startService(requireContext(), viewModel.deviceId.value, power)
        viewModel.confirmStart()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}