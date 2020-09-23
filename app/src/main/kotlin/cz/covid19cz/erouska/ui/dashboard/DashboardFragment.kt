package cz.covid19cz.erouska.ui.dashboard

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentDashboardBinding
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.main.MainVM
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard.*

@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentDashboardBinding, DashboardVM>(
    R.layout.fragment_dashboard,
    DashboardVM::class
) {

    companion object {
        const val REQUEST_GMS_ERROR_RESOLUTION = 42
    }

    private val mainViewModel: MainVM by activityViewModels()

    private val compositeDisposable = CompositeDisposable()
    private lateinit var rxPermissions: RxPermissions
    private var demoMode = false

    private val btAndLocationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                if (!it.isBtEnabled() || !it.isLocationEnabled()) {
                    safeNavigate(R.id.action_nav_dashboard_to_nav_permission_disabled, R.id.nav_dashboard)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle(R.string.app_name)
        rxPermissions = RxPermissions(this)
        subsribeToViewModel()

        viewModel.exposureNotificationsEnabled.observe(this, Observer {
            mainViewModel.serviceRunning.value = it
            if (it) {
                LocalNotificationsHelper.dismissNotRunningNotification(context)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        context?.registerReceiver(btAndLocationReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        context?.registerReceiver(
            btAndLocationReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
    }

    override fun onStop() {
        context?.unregisterReceiver(btAndLocationReceiver)
        super.onStop()
    }

    private fun subsribeToViewModel() {
        subscribe(DashboardCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                DashboardCommandEvent.Command.DATA_UP_TO_DATE -> {
                    LocalNotificationsHelper.dismissOudatedDataNotification(context)
                    data_notification_container.hide()
                }
                DashboardCommandEvent.Command.DATA_OBSOLETE -> data_notification_container.show()
                DashboardCommandEvent.Command.RECENT_EXPOSURE -> exposure_notification_container.show()
                DashboardCommandEvent.Command.EN_API_OFF -> showExposureNotificationsOff()
                DashboardCommandEvent.Command.NOT_ACTIVATED -> showWelcomeScreen()
                DashboardCommandEvent.Command.TURN_OFF -> LocalNotificationsHelper.showErouskaPausedNotification(context)
            }
        }
        subscribe(GmsApiErrorEvent::class) {
            try {
                startIntentSenderForResult(
                    it.status.resolution?.intentSender,
                    REQUEST_GMS_ERROR_RESOLUTION,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (t : Throwable){
                it.status.resolveUnknownGmsError(requireContext())
            }
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exposure_notification_content.text = AppConfig.encounterWarning
        exposure_notification_more_info.setOnClickListener {
            safeNavigate(DashboardFragmentDirections.actionNavDashboardToNavExposures(demo = demoMode), R.id.nav_dashboard)
        }
        exposure_notification_close.setOnClickListener {
            viewModel.acceptLastExposure()
            exposure_notification_container.hide()
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
                safeNavigate(R.id.nav_about, R.id.nav_dashboard)
                true
            }
            R.id.action_sandbox -> {
                safeNavigate(R.id.nav_sandbox, R.id.nav_dashboard)
                true
            }
            R.id.action_news -> {
                safeNavigate(R.id.nav_legacy_update_fragment, R.id.nav_dashboard)
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
        safeNavigate(R.id.action_nav_dashboard_to_nav_permission_disabled, R.id.nav_dashboard)
    }

    private fun showWelcomeScreen() {
        safeNavigate(R.id.action_nav_dashboard_to_nav_welcome_fragment, R.id.nav_dashboard)
    }

    private fun showPlayServicesUpdate() {
        safeNavigate(R.id.action_nav_dashboard_to_nav_play_services_update, R.id.nav_dashboard)
    }

}
