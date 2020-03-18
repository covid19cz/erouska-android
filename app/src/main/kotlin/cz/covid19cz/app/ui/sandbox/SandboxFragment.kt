package cz.covid19cz.app.ui.sandbox

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentSandboxBinding
import cz.covid19cz.app.service.CovidService
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.login.LoginActivity
import cz.covid19cz.app.ui.sandbox.event.ServiceCommandEvent
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_sandbox.*

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vLogin.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
        }
    }

    fun tryStartBtService() {
        if (viewModel.bluetoothRepository.hasBle(requireContext())) {
            if (!viewModel.bluetoothRepository.isBtEnabled()) {
                requestEnableBt()
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

    fun requestEnableBt() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE)
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