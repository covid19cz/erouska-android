package cz.covid19cz.app.ui.dashboard

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentBtDisabledBinding
import cz.covid19cz.app.service.CovidService.Companion.startService
import cz.covid19cz.app.service.CovidService.Companion.stopService
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.sandbox.ExportEvent
import cz.covid19cz.app.ui.dashboard.event.DashboardCommandEvent
import io.reactivex.disposables.CompositeDisposable

class DashboardFragment : BaseFragment<FragmentBtDisabledBinding, DashboardVM>(
    R.layout.fragment_dashboard,
    DashboardVM::class
) {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermissions = RxPermissions(this)

        subscribe(DashboardCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                DashboardCommandEvent.Command.TURN_ON -> tryStartBtService()
                DashboardCommandEvent.Command.TURN_OFF -> context?.let { stopService(it) }
                DashboardCommandEvent.Command.UPLOAD -> TODO()
                DashboardCommandEvent.Command.SHOW_DATA -> TODO()
                DashboardCommandEvent.Command.SHARE -> TODO()
            }
        }

        subscribe(ExportEvent.Complete::class) { event ->
            view?.let {
                Snackbar.make(it, event.fileName, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.help, menu)
        inflater.inflate(R.menu.sandbox, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun share() {
        TODO()
    }

    override fun onBluetoothEnabled() {
        navController().navigateUp()
    }

    private fun tryStartBtService() {
        if (viewModel.bluetoothRepository.hasBle(requireContext())) {
            if (!viewModel.bluetoothRepository.isBtEnabled()) {
                navigate(R.id.action_nav_sandbox_to_nav_bt_disabled)
                return
            }
            compositeDisposable.add(rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe { granted: Boolean ->
                    if (granted) {
                        startService(requireContext())
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

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}