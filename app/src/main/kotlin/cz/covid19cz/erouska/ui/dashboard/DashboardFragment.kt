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
import cz.covid19cz.erouska.databinding.FragmentDashboardPlusBinding
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsErrorHandling
import cz.covid19cz.erouska.exposurenotifications.Notifications
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.exposure.event.ExposuresCommandEvent
import cz.covid19cz.erouska.ui.main.MainVM
import cz.covid19cz.erouska.utils.showOrHide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard_cards.*
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentDashboardPlusBinding, DashboardVM>(
    R.layout.fragment_dashboard_plus,
    DashboardVM::class
) {

    companion object {
        private const val SCREEN_NAME = "Dashboard"
    }

    private val mainViewModel: MainVM by activityViewModels()

    @Inject
    lateinit var notifications: Notifications
    @Inject
    internal lateinit var exposureNotificationsErrorHandling: ExposureNotificationsErrorHandling

    private lateinit var rxPermissions: RxPermissions
    private var demoMode = false

    private val btAndLocationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                viewModel.checkStatus()
                refreshDotIndicator(context)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle(R.string.app_name)
        rxPermissions = RxPermissions(this)
        subscribeToViewModel()

        viewModel.exposureNotificationsEnabled.observe(this, Observer { isEnabled ->
            refreshDotIndicator(requireContext())
            if (isEnabled) {
                notifications.dismissNotRunningNotification()
            }
            checkAppActive()
        })

        viewModel.bluetoothState.observe(
            this,
            Observer { isEnabled -> onBluetoothStateChanged(isEnabled) })

        viewModel.locationState.observe(
            this,
            Observer { isEnabled -> onLocationStateChanged(isEnabled) })

        viewModel.lastUpdateTime.observe(this, Observer { updateLastUpdateDateAndTime() })
        viewModel.lastExposureDate.observe(this, Observer { updateLastUpdateDateAndTime() })
    }

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)

            // don't register Location Receiver on devices with Android 11+ (it's not mandatory to have location services turned on)
            // https://developer.android.com/about/versions/11/behavior-changes-all#exposure-notifications
            // https://developers.google.com/android/exposure-notifications/implementation-guide#locationless_scanning_in_android_11
            if (!viewModel.isLocationlessScanSupported()) {
                addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            }
        }

        context?.registerReceiver(
            btAndLocationReceiver,
            intentFilter
        )
    }

    override fun onStop() {
        context?.unregisterReceiver(btAndLocationReceiver)
        super.onStop()
    }

    private fun refreshDotIndicator(context: Context) {
        mainViewModel.serviceRunning.value =
            viewModel.exposureNotificationsEnabled.value &&
                    context.isBtEnabled() &&
                    (viewModel.isLocationlessScanSupported() || context.isLocationEnabled())
    }

    private fun subscribeToViewModel() {
        subscribe(DashboardCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                DashboardCommandEvent.Command.DATA_UP_TO_DATE -> {
                    notifications.dismissOudatedDataNotification()
                    data_notification_container.hide()
                }
                DashboardCommandEvent.Command.DATA_OBSOLETE -> data_notification_container.show()
                DashboardCommandEvent.Command.RECENT_EXPOSURE -> exposure_notification_container.show()
                DashboardCommandEvent.Command.NOT_ACTIVATED -> showWelcomeScreen()
                DashboardCommandEvent.Command.TURN_OFF -> notifications.showErouskaPausedNotification()
            }
        }
        subscribe(GmsApiErrorEvent::class) {
            exposureNotificationsErrorHandling.handle(it, this, SCREEN_NAME)
        }

        subscribe(ExposuresCommandEvent::class) {
            when (it.command) {
                ExposuresCommandEvent.Command.RECENT_EXPOSURE -> onRecentExposureDiscovered()
                ExposuresCommandEvent.Command.NO_RECENT_EXPOSURES -> onNoExposureDiscovered()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exposure_notification_content.text = AppConfig.encounterWarning
        exposure_notification_close.setOnClickListener {
            viewModel.acceptExposure()
            exposure_notification_container.hide()
        }
        data_notification_close.setOnClickListener { data_notification_container.hide() }
        enableUpInToolbar(false)

        data_notification_content.text = AppConfig.recentExposureNotificationTitle

        dash_card_no_risky_encounter.card_title = AppConfig.noEncounterCardTitle

        dash_bluetooth_off.card_on_content_click = View.OnClickListener { requestEnableBt() }
        dash_location_off.card_on_content_click = View.OnClickListener { requestLocationEnable() }

        dash_card_risky_encounter.card_on_content_click =
            View.OnClickListener { viewModel.showExposureDetail() }
        dash_card_no_risky_encounter.card_on_content_click =
            View.OnClickListener { viewModel.showExposureDetail() }

        dash_card_positive_test.card_on_content_click =
            View.OnClickListener { viewModel.sendData() }
        
        exposure_notification_content.text = AppConfig.encounterWarning
        exposure_notification_more_info.setOnClickListener { viewModel.showExposureDetail() }
        exposure_notification_close.setOnClickListener {
            viewModel.acceptExposure()
            exposure_notification_container.hide()
        }

        data_notification_close.setOnClickListener { data_notification_container.hide() }

        updateLastUpdateDateAndTime()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard, menu)
        if (BuildConfig.FLAVOR == "dev") {
            menu.add(0, R.id.action_news, 10, "Test Novinky")
            menu.add(0, R.id.action_activation, 11, "Test Aktivace")
            menu.add(0, R.id.action_exposure_demo, 12, "Test Riz. Notifikace")
            menu.add(0, R.id.action_play_services, 13, "Test PlayServices")
            menu.add(0, R.id.action_sandbox, 14, "Test Sandbox")
            menu.add(0, R.id.action_dashboard_cards, 16, "Test Dashboard Cards")
            menu.add(0, R.id.action_exposure_screen, 17, "Test Exposure screen")
            menu.add(0, R.id.action_exposure_info, 18, "Test Rizikové setkání")
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
            R.id.action_exposure_screen -> {
                navigate(DashboardFragmentDirections.actionNavDashboardToNavExposure(demo = true))
                true
            }
            R.id.action_play_services -> {
                showPlayServicesUpdate()
                true
            }
            R.id.action_dashboard_cards -> {
                showDashboardCards()
                true
            }
            R.id.action_exposure_info -> {
                navigate(DashboardFragmentDirections.actionNavDashboardToNavExposureInfo(demo = true))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ExposureNotificationsErrorHandling.REQUEST_GMS_ERROR_RESOLUTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.start()
                }
            }
        }
    }

    private fun updateLastUpdateDateAndTime() {
        val lastUpdateString =
            if (viewModel.lastUpdateTime.value != null && viewModel.lastUpdateDate.value != null) {
                "${
                    resources.getString(
                        R.string.dashboard_body_no_contact,
                        viewModel.lastUpdateDate.value,
                        viewModel.lastUpdateTime.value
                    )
                }\n${AppConfig.encounterUpdateFrequency}"
            } else {
                null
            }

        if (viewModel.lastExposureDate.value != null) {
            updateLastUpdateOnExposureCard(lastUpdateString)
        } else {
            dash_card_no_risky_encounter.card_subtitle = lastUpdateString.orEmpty()
        }
    }

    private fun updateLastUpdateOnExposureCard(lastUpdateString: String?) {
        val lastExposureString = resources.getString(
            R.string.dashboard_risky_encounter_subtitle_bad,
            viewModel.lastExposureDate.value
        )

        if (lastUpdateString != null) {
            dash_card_risky_encounter.card_subtitle = "${lastExposureString}\n\n${lastUpdateString}"
        } else {
            dash_card_risky_encounter.card_subtitle = lastExposureString
        }
    }

    private fun onRecentExposureDiscovered() {
        dash_card_no_risky_encounter.hide()
        dash_card_risky_encounter.show()
    }

    private fun onNoExposureDiscovered() {
        dash_card_no_risky_encounter.show()
        dash_card_risky_encounter.hide()
    }

    private fun onBluetoothStateChanged(isEnabled: Boolean) {
        dash_bluetooth_off.showOrHide(!isEnabled)
        checkAppActive()
    }

    private fun onLocationStateChanged(isEnabled: Boolean) {
        // Location services don't need to be turned on on devices with Android 11+
        if (viewModel.isLocationlessScanSupported()) {
            dash_location_off.hide()
        } else {
            dash_location_off.showOrHide(!isEnabled)
        }
        checkAppActive()
    }

    private fun checkAppActive() {
        val enEnabled = viewModel.exposureNotificationsEnabled.value
        val lsEnabled = viewModel.locationState.value // Location services don't need to be turned on on devices with Android 11+
        val btEnabled = viewModel.bluetoothState.value

        dash_card_active.showOrHide( enEnabled && (viewModel.isLocationlessScanSupported() || lsEnabled) && btEnabled)
        dash_card_inactive.showOrHide(!enEnabled && (viewModel.isLocationlessScanSupported() || lsEnabled) && btEnabled)
    }

    private fun showWelcomeScreen() {
        navigate(R.id.action_nav_dashboard_to_nav_welcome_fragment)
    }

    private fun showPlayServicesUpdate() {
        navigate(R.id.action_nav_dashboard_to_nav_play_services_update, Bundle().apply { putBoolean("demo", true) })
    }

    private fun showDashboardCards() {
        navigate(R.id.action_nav_dashboard_to_nav_dashboard_cards)
    }
}
