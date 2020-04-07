package cz.covid19cz.erouska.ui.sandbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import arch.livedata.SafeMutableLiveData
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSandboxBinding
import cz.covid19cz.erouska.ext.getLocationPermission
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.service.CovidService
import cz.covid19cz.erouska.service.CovidService.Companion.ACTION_TUID_ROTATED
import cz.covid19cz.erouska.service.CovidService.Companion.EXTRA_TUID
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.utils.L
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject

class SandboxFragment :
    BaseFragment<FragmentSandboxBinding, SandboxVM>(R.layout.fragment_sandbox, SandboxVM::class) {

    companion object {
        const val REQUEST_BT_ENABLE = 1000
    }

    private val localBroadcastManager by inject<LocalBroadcastManager>()

    private val tuidRotationReceiver = object :BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.let {
                if (it == ACTION_TUID_ROTATED) {
                    viewModel.tuid.postValue(intent.getStringExtra(EXTRA_TUID) ?: "")
                }
            }
        }

    }

    private lateinit var rxPermissions: RxPermissions
    private val compositeDisposable = CompositeDisposable()
    val serviceRunning = SafeMutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermissions = RxPermissions(this)
        localBroadcastManager.registerReceiver(
            tuidRotationReceiver,
            IntentFilter(
            ACTION_TUID_ROTATED)
        )

        subscribe(DashboardCommandEvent::class) {
            when (it.command) {
                DashboardCommandEvent.Command.TURN_ON -> tryStartBtService()
                DashboardCommandEvent.Command.TURN_OFF -> stopService()
            }
        }

        if (CovidService.isRunning(requireContext())) {
            L.d("Service Covid is running")
            viewModel.serviceRunning.value = true
        } else {
            L.d("Service Covid is not running")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)
        binding.easter.hide()
    }

    override fun onBluetoothEnabled() {
        super.onBluetoothEnabled()
        tryStartBtService()
    }

    private fun tryStartBtService() {
        if (viewModel.bluetoothRepository.hasBle(requireContext())) {
            if (!viewModel.bluetoothRepository.isBtEnabled()) {
                navigate(R.id.action_nav_sandbox_to_nav_bt_disabled)
                return
            }
            compositeDisposable.add(rxPermissions
                .request(getLocationPermission())
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

    private fun stopService() {
        with(requireContext()){
            startService(CovidService.stopService(this))
        }
        binding.easter.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_BT_ENABLE -> {
                tryStartBtService()
            }
        }
    }

    private fun startBtService() {
        with(requireContext()) {
            ContextCompat.startForegroundService(this, CovidService.startService(this))
        }
        viewModel.confirmStart()
        binding.easter.hide()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        localBroadcastManager.unregisterReceiver(tuidRotationReceiver)
        super.onDestroy()
    }
}