package cz.covid19cz.erouska.ui.dashboard

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.*
import cz.covid19cz.erouska.ui.main.MainVM
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class DashboardFragment : BaseFragment<FragmentPermissionssDisabledBinding, DashboardVM>(
    R.layout.fragment_dashboard,
    DashboardVM::class
) {

    companion object {
        const val REQUEST_GMS_ERROR_RESOLUTION = 42
    }

    private val mainViewModel: MainVM by sharedViewModel()

    private val compositeDisposable = CompositeDisposable()
    private lateinit var rxPermissions: RxPermissions
    var demoMode = false

    private val btReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                val btDisabled = !it.isBtEnabled()
                val locationDisabled = !it.isLocationProvided()

                if (btDisabled || locationDisabled) {
                    navigate(R.id.action_nav_dashboard_to_nav_bt_disabled)
                    it.unregisterReceiver(this)
                    it.unregisterReceiver(locationReceiver)
                }
            }
        }
    }

    private val locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                val btDisabled = !it.isBtEnabled()
                val locationDisabled = !it.isLocationProvided()

                if (btDisabled || locationDisabled) {
                    navigate(R.id.action_nav_dashboard_to_nav_bt_disabled)
                    it.unregisterReceiver(btReceiver)
                    it.unregisterReceiver(this)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle(R.string.app_name)
        rxPermissions = RxPermissions(this)
        subsribeToViewModel()

        viewModel.serviceRunning.observe(this, Observer {
            mainViewModel.serviceRunning.value = it
            if (it) {
                LocalNotificationsHelper.dismissNotRunningNotification(context)
            }
        })
    }

    override fun onStart() {
        super.onStart()

        context?.let {
            val btDisabled = !it.isBtEnabled()
            val locationDisabled = !it.isLocationProvided()

            if (btDisabled || locationDisabled) {
                navigate(R.id.action_nav_dashboard_to_nav_bt_disabled)
                return
            }
        }

        context?.registerReceiver(btReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        context?.registerReceiver(locationReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
    }

    private fun subsribeToViewModel() {
        subscribe(DashboardCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                DashboardCommandEvent.Command.DATA_OBSOLETE -> data_notification_container.show()
                DashboardCommandEvent.Command.RECENT_EXPOSURE -> exposure_notification_container.show()
                DashboardCommandEvent.Command.EN_API_OFF -> showExposureNotificationsOff()
                DashboardCommandEvent.Command.NOT_ACTIVATED -> showWelcomeScreen()
                DashboardCommandEvent.Command.TURN_OFF -> LocalNotificationsHelper.showErouskaPausedNotification(context)
            }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exposure_notification_content.text = AppConfig.encounterWarning
        exposure_notification_close.setOnClickListener { exposure_notification_container.hide() }
        exposure_notification_more_info.setOnClickListener {
            navigate(
                DashboardFragmentDirections.actionNavDashboardToNavExposures(
                    demo = demoMode
                )
            )
        }
        data_notification_close.setOnClickListener { data_notification_container.hide() }

        enableUpInToolbar(false)

        data_notification_close.setOnClickListener { data_notification_container.hide() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard, menu)
        if (BuildConfig.FLAVOR == "dev") {
            menu.add(0, R.id.action_news, 10, "Test Novinky")
            menu.add(0, R.id.action_activation, 11, "Test Aktivace")
            menu.add(0, R.id.action_exposure_demo, 12, "Test Rizikové setkání")
            menu.add(0, R.id.action_play_services, 13, "Test PlayServices")
            menu.add(0, R.id.action_sandbox, 14, "Test Sandbox")
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
            R.id.action_activation -> {
                viewModel.unregister()
                showWelcomeScreen()
                true
            }
            R.id.action_exposure_demo -> {
                demoMode = true
                exposure_notification_container.show()
                true
            }
            R.id.action_play_services -> {
                showPlayServicesUpdate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
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

    private fun showWelcomeScreen() {
        navigate(R.id.action_nav_dashboard_to_nav_welcome_fragment)
    }

    private fun showPlayServicesUpdate() {
        navigate(R.id.action_nav_dashboard_to_nav_play_services_update)
    }

}
