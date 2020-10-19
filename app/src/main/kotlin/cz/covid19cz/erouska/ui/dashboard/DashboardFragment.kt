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
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.exposure.event.ExposuresCommandEvent
import cz.covid19cz.erouska.ui.main.MainVM
import cz.covid19cz.erouska.ui.permissions.bluetooth.event.PermissionsEvent
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard_cards.*

@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentDashboardPlusBinding, DashboardVM>(
    R.layout.fragment_dashboard_plus,
    DashboardVM::class
) {

    private val mainViewModel: MainVM by activityViewModels()

    private val compositeDisposable = CompositeDisposable()
    private lateinit var rxPermissions: RxPermissions
    private var demoMode = false

    private val btAndLocationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                refreshDotIndicator()
                viewModel.onPermissionsStateChanged(it.isBtEnabled(), it.isLocationEnabled())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle(R.string.app_name)
        rxPermissions = RxPermissions(this)
        subsribeToViewModel()

        viewModel.exposureNotificationsEnabled.observe(this, Observer { enEnabled ->
            refreshDotIndicator()
            if (enEnabled) {
                LocalNotificationsHelper.dismissNotRunningNotification(context)
            }
        })

        viewModel.permissionsState.observe(this, Observer { permissionsState ->
            when (permissionsState) {
                DashboardVM.PermissionsState.BOTH_ENABLED -> onBtAndLocationEnabled()
                DashboardVM.PermissionsState.LOCATION_DISABLED -> onOnlyLocationDisabled()
                DashboardVM.PermissionsState.BT_DISABLED -> onOnlyBtDisabled()
                DashboardVM.PermissionsState.BOTH_DISABLED -> onBothBtAndLocationDisabled()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        context?.registerReceiver(
            btAndLocationReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        context?.registerReceiver(
            btAndLocationReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )

    }

    override fun onStop() {
        context?.unregisterReceiver(btAndLocationReceiver)
        super.onStop()
    }

    private fun refreshDotIndicator() {
        mainViewModel.serviceRunning.value =
            viewModel.exposureNotificationsEnabled.value && requireContext().isBtEnabled() && requireContext().isLocationEnabled()
    }

    private fun subsribeToViewModel() {
        subscribe(DashboardCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                DashboardCommandEvent.Command.DATA_UP_TO_DATE -> {
//                    LocalNotificationsHelper.dismissOudatedDataNotification(context)
//                    data_notification_container.hide()
                }
                DashboardCommandEvent.Command.DATA_OBSOLETE -> data_notification_container.show()
                DashboardCommandEvent.Command.RECENT_EXPOSURE -> exposure_notification_container.show()
                DashboardCommandEvent.Command.EN_API_OFF -> showExposureNotificationsOff()
                DashboardCommandEvent.Command.NOT_ACTIVATED -> showWelcomeScreen()
                DashboardCommandEvent.Command.TURN_OFF -> LocalNotificationsHelper.showErouskaPausedNotification(
                    context
                )
            }
        }
        subscribe(GmsApiErrorEvent::class) {
            ExposureNotificationsErrorHandling.handle(it, this)
        }
        subscribe(PermissionsEvent::class) {
            when (it.command) {
                PermissionsEvent.Command.ENABLE_BT -> requestEnableBt()
                PermissionsEvent.Command.ENABLE_LOCATION -> requestLocationEnable()
                PermissionsEvent.Command.ENABLE_BT_LOCATION -> requestLocationEnable()
            }
        }
        subscribe(ExposuresCommandEvent::class) {
            when (it.command) {
                ExposuresCommandEvent.Command.RECENT_EXPOSURE -> onRecentExposureDiscovered()
                ExposuresCommandEvent.Command.NO_RECENT_EXPOSURES -> onNoExposureDiscovered()
            }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exposure_notification_content.text = AppConfig.encounterWarning
        exposure_notification_close.setOnClickListener {
            viewModel.acceptExposure()
            exposure_notification_container.hide()
        }
        data_notification_close.setOnClickListener { data_notification_container.hide() }
        enableUpInToolbar(false)

        // TODO Change RC string (in defaults and on server side) to march Figma
        dash_card_no_risky_encounter.card_title = AppConfig.noEncounterHeader
        dash_card_no_risky_encounter.card_subtitle = resources.getString(
            R.string.dashboard_body_no_contact,
            viewModel.lastUpdateDate.value,
            viewModel.lastUpdateTime.value
        ) + "\n" + AppConfig.encounterUpdateFrequency

        // Samples
        dash_bluetooth_off.card_on_button_click = View.OnClickListener { requestEnableBt() }
        dash_location_off.card_on_button_click = View.OnClickListener { requestLocationEnable() }
        dash_bluetooth_location_off.card_on_button_click =
            View.OnClickListener { requestLocationEnable() }

        dash_card_risky_encounter.card_on_content_click =
            View.OnClickListener { navigate(R.id.action_nav_dashboard_to_nav_exposures) }
        dash_card_no_risky_encounter.card_on_content_click =
            View.OnClickListener { navigate(R.id.action_nav_dashboard_to_nav_exposures) }

//        exposure_notification_content.text = AppConfig.encounterWarning
//        exposure_notification_more_info.setOnClickListener {
//            navigate(DashboardFragmentDirections.actionNavDashboardToNavExposures(demo = demoMode))
//        }
//        exposure_notification_close.setOnClickListener {
//            viewModel.acceptExposure()
//            exposure_notification_container.hide()
//        }
//        exposure_notification_more_info.setOnClickListener { navigate(DashboardFragmentDirections.actionNavDashboardToNavExposures(demo = demoMode)) }
//        data_notification_close.setOnClickListener { data_notification_container.hide() }
//
//        enableUpInToolbar(false)
//
//        data_notification_close.setOnClickListener { data_notification_container.hide() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard, menu)
        if (BuildConfig.FLAVOR == "dev") {
            menu.add(0, R.id.action_news, 10, "Test Novinky")
            menu.add(0, R.id.action_activation, 11, "Test Aktivace")
            menu.add(0, R.id.action_exposure_demo, 12, "Test Rizikové setkání")
            menu.add(0, R.id.action_play_services, 13, "Test PlayServices")
            menu.add(0, R.id.action_sandbox, 14, "Test Sandbox")
            menu.add(0, R.id.action_dashboard_cards, 16, "Test Dashboard Cards")
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
            R.id.action_dashboard_cards -> {
                showDashboardCards()
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
            ExposureNotificationsErrorHandling.REQUEST_GMS_ERROR_RESOLUTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.start()
                }
            }
        }
    }

    private fun onBothBtAndLocationDisabled() {
        dash_bluetooth_location_off.show()
        dash_bluetooth_off.hide()
        dash_location_off.hide()
    }

    private fun onOnlyBtDisabled() {
        dash_bluetooth_location_off.hide()
        dash_bluetooth_off.show()
        dash_location_off.hide()
    }

    private fun onOnlyLocationDisabled() {
        dash_bluetooth_location_off.hide()
        dash_bluetooth_off.hide()
        dash_location_off.show()
    }

    private fun onBtAndLocationEnabled() {
        dash_bluetooth_location_off.hide()
        dash_bluetooth_off.hide()
        dash_location_off.hide()
    }

    private fun showExposureNotificationsOff() {
        L.d("show exposure notifications off")
        navigate(R.id.action_nav_dashboard_to_nav_permission_disabled)
    }

    private fun showWelcomeScreen() {
        navigate(R.id.action_nav_dashboard_to_nav_welcome_fragment)
    }

    private fun showPlayServicesUpdate() {
        navigate(R.id.action_nav_dashboard_to_nav_play_services_update)
    }

    private fun showDashboardCards() {
        navigate(R.id.action_nav_dashboard_to_nav_dashboard_cards)
    }

}
