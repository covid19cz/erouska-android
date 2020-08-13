package cz.covid19cz.erouska.ui.dashboard

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
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
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.BluetoothDisabledEvent
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.android.ext.android.inject


class DashboardFragment : BaseFragment<FragmentPermissionssDisabledBinding, DashboardVM>(
    R.layout.fragment_dashboard,
    DashboardVM::class
) {

    companion object {
        const val REQUEST_GMS_ERROR_RESOLUTION = 42
    }

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
                DashboardCommandEvent.Command.DATA_OBSOLETE -> data_notification_container.show()
                DashboardCommandEvent.Command.RECENT_EXPOSURE -> exposure_notification_container.show()
                DashboardCommandEvent.Command.EN_API_OFF -> showExposureNotificationsOff()
            }
            subscribe(BluetoothDisabledEvent::class) {
                navigate(R.id.action_nav_dashboard_to_nav_bt_disabled)
            }
            subscribe(GmsApiErrorEvent::class) {
                startIntentSenderForResult(
                    it.status.resolution?.intentSender,
                    REQUEST_GMS_ERROR_RESOLUTION,
                    null,
                    0,
                    0,
                    0,
                    null
                )
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

        if (BuildConfig.FLAVOR == "dev") {
            debug_buttons_container.show()
        }

        exposure_notification_content.text = AppConfig.exposureNotificationContent
        exposure_notification_close.setOnClickListener { exposure_notification_container.hide() }
        exposure_notification_more_info.setOnClickListener { navigate(R.id.action_nav_dashboard_to_nav_exposures) }
        data_notification_close.setOnClickListener { data_notification_container.hide() }

        enableUpInToolbar(false)

        data_notification_close.setOnClickListener { data_notification_container.hide() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard, menu)
        if (BuildConfig.DEBUG) {
            menu.add(0, R.id.action_sandbox, 999, "Test")
            menu.add(0, R.id.action_news, 666, "TestNovinky")
        }
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
            R.id.action_sandbox -> {
                navigate(R.id.nav_sandbox)
                true
            }
            R.id.action_news -> {
                navigate(R.id.nav_legacy_update_fragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun resumeService() {
        viewModel.start()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_GMS_ERROR_RESOLUTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.start()
                }
            }
        }
    }

    private fun showExposureNotificationsOff() {
        navigate(R.id.action_nav_dashboard_to_nav_bt_disabled)
    }

}
