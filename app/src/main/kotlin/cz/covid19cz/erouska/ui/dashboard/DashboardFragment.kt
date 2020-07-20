package cz.covid19cz.erouska.ui.dashboard

import android.content.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.android.ext.android.inject


class DashboardFragment : BaseFragment<FragmentPermissionssDisabledBinding, DashboardVM>(
    R.layout.fragment_dashboard,
    DashboardVM::class
) {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var rxPermissions: RxPermissions
    private val localBroadcastManager by inject<LocalBroadcastManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle(R.string.app_name)
        rxPermissions = RxPermissions(this)
        subsribeToViewModel()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateState()
    }

    private fun subsribeToViewModel() {
        subscribe(DashboardCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                DashboardCommandEvent.Command.TURN_ON -> tryStartBtService()
                DashboardCommandEvent.Command.TURN_OFF -> stopService()
            }
        }
    }

    private fun updateState() {
        checkRequirements(onFailed = {
            navigate(R.id.action_nav_dashboard_to_nav_bt_disabled)
        }, onBatterySaverEnabled = {
            showBatterySaverDialog()
        })
    }

    private fun showBatterySaverDialog() {
        MaterialAlertDialogBuilder(context)
            .setMessage(R.string.battery_saver_disabled_desc)
            .setPositiveButton(R.string.disable_battery_saver)
            { dialog, which ->
                dialog.dismiss()
                navigateToBatterySaverSettings {
                    showSnackBar(R.string.battery_saver_settings_not_found)
                }
            }
            .setNegativeButton(getString(R.string.confirmation_button_close))
            { dialog, which -> dialog.dismiss() }
            .show()
    }

    private fun navigateToBatterySaverSettings(onBatterySaverNotFound: () -> Unit) {
        val batterySaverIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        } else {
            val intent = Intent()
            intent.component = ComponentName(
                "com.android.settings",
                "com.android.settings.Settings\$BatterySaverSettingsActivity"
            )
            intent
        }
        try {
            startActivity(batterySaverIntent)
        } catch (ex: ActivityNotFoundException) {
            onBatterySaverNotFound()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)
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

    override fun onBluetoothEnabled() {
        tryStartBtService()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun resumeService() {
        //TODO: Implement
    }

    private fun pauseService() {
        //TODO: Implement
    }

    private fun stopService() {
        //TODO: Implement
    }

    private fun checkRequirements(
        onPassed: () -> Unit = {},
        onFailed: () -> Unit = {},
        onBatterySaverEnabled: () -> Unit = {}
    ) {
        with(requireContext()) {
            if (!isBtEnabled()) {
                onFailed()
                return
            } else if (isBatterySaverEnabled()) {
                onBatterySaverEnabled()
            } else {
                onPassed()
            }
        }
    }

    private fun tryStartBtService() {
        checkRequirements(
            {
                resumeService()
            },
            { navigate(R.id.action_nav_dashboard_to_nav_bt_disabled) },
            { showBatterySaverDialog() }
        )
    }
}
