package cz.covid19cz.app.ui.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentBtDisabledBinding
import cz.covid19cz.app.ext.getLocationPermission
import cz.covid19cz.app.service.CovidService
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.sandbox.ExportEvent
import cz.covid19cz.app.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.app.utils.L
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject

class DashboardFragment : BaseFragment<FragmentBtDisabledBinding, DashboardVM>(
    R.layout.fragment_dashboard,
    DashboardVM::class
) {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var rxPermissions: RxPermissions
    private val localBroadcastManager by inject<LocalBroadcastManager>()

    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    CovidService.ACTION_MASK_STARTED -> viewModel.serviceRunning.value = true
                    CovidService.ACTION_MASK_STOPPED -> viewModel.serviceRunning.value = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerServiceStateReceivers()

        rxPermissions = RxPermissions(this)

        subscribe(DashboardCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                DashboardCommandEvent.Command.TURN_ON -> tryStartBtService()
                DashboardCommandEvent.Command.TURN_OFF -> context?.let {
                    it.startService(CovidService.stopService(it))
                }
                DashboardCommandEvent.Command.UPLOAD -> TODO()
                DashboardCommandEvent.Command.SHOW_DATA -> TODO()
                DashboardCommandEvent.Command.SHARE -> showSnackBar("Sdílet zatím neumím.")
                DashboardCommandEvent.Command.PAUSE -> pauseService()
                DashboardCommandEvent.Command.RESUME -> resumeService()
            }
        }

        subscribe(ExportEvent.Complete::class) { event ->
            view?.let {
                Snackbar.make(it, event.fileName, Snackbar.LENGTH_LONG).show()
            }
        }

        checkIfServiceIsRunning()

        viewModel.init()
    }

    private fun checkIfServiceIsRunning() {
        if (CovidService.isRunning(requireContext())) {
            L.d("Service Covid is running")
            viewModel.serviceRunning.value = true
        } else {
            viewModel.serviceRunning.value = false
            L.d("Service Covid is not running")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sandbox, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onBluetoothEnabled() {
        tryStartBtService()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        localBroadcastManager.unregisterReceiver(serviceStateReceiver)
        super.onDestroy()
    }

    private fun resumeService() {
        requireContext().run {
            startService(CovidService.resume(this))
        }
    }

    private fun pauseService() {
        requireContext().run {
            startService(CovidService.pause(this))
        }
    }

    private fun registerServiceStateReceivers() {
        localBroadcastManager.registerReceiver(
            serviceStateReceiver,
            IntentFilter(CovidService.ACTION_MASK_STARTED)
        )
        localBroadcastManager.registerReceiver(
            serviceStateReceiver,
            IntentFilter(CovidService.ACTION_MASK_STOPPED)
        )
    }

    private fun tryStartBtService() {
        if (viewModel.bluetoothRepository.hasBle(requireContext())) {
            if (!viewModel.bluetoothRepository.isBtEnabled()) {
                navigate(R.id.action_nav_dashboard_to_nav_bt_disabled)
                return
            }
            compositeDisposable.add(rxPermissions
                .request(getLocationPermission())
                .subscribe { granted: Boolean ->
                    if (granted) {
                        with (requireContext()) {
                            ContextCompat.startForegroundService(this, CovidService.startService(this))
                        }
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
}