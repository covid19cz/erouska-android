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
import androidx.appcompat.content.res.AppCompatResources.getDrawable
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
import cz.covid19cz.erouska.ui.main.MainVM
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dashboard.*

@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentDashboardPlusBinding, DashboardVM>(
    R.layout.fragment_dashboard_plus,
    DashboardVM::class
) {

    private val mainViewModel: MainVM by activityViewModels()

    private lateinit var rxPermissions: RxPermissions
    private var demoMode = false

    private val btAndLocationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                viewModel.bluetoothState.value = context.isBtEnabled()
                viewModel.locationState.value = context.isLocationEnabled()
                refreshDotIndicator(context)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle(R.string.app_name)
        rxPermissions = RxPermissions(this)

        initializeCards()
        subscribeToViewModel()
        subscribeEnApi()
        subscribeBluetooth()
        subscribeLocationServices()
        subscribeRiskyEncounter()

    }

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
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
                    context.isLocationEnabled()
    }

    private fun subscribeToViewModel() {
        subscribe(DashboardCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                DashboardCommandEvent.Command.DATA_UP_TO_DATE -> {
                    LocalNotificationsHelper.dismissOudatedDataNotification(context)
                    data_notification_container?.hide()
                }
                DashboardCommandEvent.Command.DATA_OBSOLETE -> data_notification_container.show()
                DashboardCommandEvent.Command.RECENT_EXPOSURE -> exposure_notification_container.show()
                DashboardCommandEvent.Command.NOT_ACTIVATED -> showWelcomeScreen()
                DashboardCommandEvent.Command.TURN_OFF ->
                    LocalNotificationsHelper.showErouskaPausedNotification(context)
                DashboardCommandEvent.Command.ENABLE_BT -> requestEnableBt()
                DashboardCommandEvent.Command.ENABLE_LOCATION_SERVICES -> requestLocationEnable()
            }
        }
        subscribe(GmsApiErrorEvent::class) {
            ExposureNotificationsErrorHandling.handle(it, this)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(false)

        data_notification_close.setOnClickListener { data_notification_container.hide() }

        exposure_notification_content.text = AppConfig.encounterWarning
        exposure_notification_more_info.setOnClickListener {
            navigate(DashboardFragmentDirections.actionNavDashboardToNavExposures(demo = demoMode))
        }
        exposure_notification_close.setOnClickListener {
            viewModel.acceptExposure()
            exposure_notification_container.hide()
        }
        exposure_notification_more_info.setOnClickListener {
            navigate(
                DashboardFragmentDirections.actionNavDashboardToNavExposures(
                    demo = demoMode
                )
            )
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ExposureNotificationsErrorHandling.REQUEST_GMS_ERROR_RESOLUTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.start()
                }
            }
        }
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

    private fun initializeCards() {
        Type.values().forEach {
            L.i("initializing $it")
            val dashboardCard = DashboardCard(it)
            dashboardCard.title.value = getString(it.title)
            dashboardCard.subtitle.value = getString(it.subtitle)
            dashboardCard.icon.value = getDrawable(requireContext(), it.icon)
            viewModel.allCards[it] = dashboardCard
        }
    }

    private fun subscribeEnApi() {
        viewModel.exposureNotificationsEnabled.observe(this, Observer { isEnabled ->
            refreshDotIndicator(requireContext())
            setActiveCardVisibility(isEnabled)
        })
    }

    private fun subscribeBluetooth() {
        viewModel.bluetoothState.observe(this, Observer { isEnabled ->

            if (isEnabled) {
                viewModel.removeCard(viewModel.allCards[Type.BLUETOOTH])
            } else {
                viewModel.addCard(viewModel.allCards[Type.BLUETOOTH])
            }

            setActiveCardVisibility(isEnabled && viewModel.locationState.value)

        })
    }

    private fun subscribeLocationServices() {
        viewModel.locationState.observe(this, Observer { isEnabled ->

            if (isEnabled) {
                viewModel.removeCard(viewModel.allCards[Type.LOCATION_SERVICES])
            } else {
                viewModel.addCard(viewModel.allCards[Type.LOCATION_SERVICES])
            }

            setActiveCardVisibility(isEnabled && viewModel.bluetoothState.value)

        })
    }

    private fun setActiveCardVisibility(isActive: Boolean) {
        if (isActive) {
            viewModel.removeCard(viewModel.allCards[Type.INACTIVE_APP])
            viewModel.addCard(viewModel.allCards[Type.ACTIVE_APP])
        } else {
            viewModel.removeCard(viewModel.allCards[Type.ACTIVE_APP])
            viewModel.addCard(viewModel.allCards[Type.INACTIVE_APP])
        }
    }

    private fun subscribeRiskyEncounter() {
        val riskyDashboardCard = viewModel.allCards[Type.RISKY_ENCOUNTER]

        viewModel.lastExposureDate.observe(this, Observer {
            L.i("Last exposure date updated to $it")
            updateRiskyEncountersCard(viewModel.exposuresCount.value ?: 0 > 0)
        })

        viewModel.exposuresCount.observe(this, Observer { exposuresCount ->
            L.i("exposures count updated $exposuresCount")
            val hasExposures = exposuresCount > 0
            updateRiskyEncountersCard(hasExposures)
            riskyDashboardCard?.isAlert?.value = hasExposures
        })

        viewModel.lastUpdateDate.observe(this, Observer {
            L.i("Last update date update to $it")
            updateRiskyEncountersCard(viewModel.exposuresCount.value ?: 0 > 0)
        })
    }

    private fun updateRiskyEncountersCard(hasRiskyEncounters: Boolean) {
        if (hasRiskyEncounters) {
            setSomeRiskyEncounters()
        } else {
            setNoRiskyEncounters()
        }
    }

    private fun setNoRiskyEncounters() {
        val riskyDashboardCard = viewModel.allCards[Type.RISKY_ENCOUNTER]

        riskyDashboardCard?.title?.value =
            resources.getString(R.string.dashboard_risky_encounter_title_ok)

        riskyDashboardCard?.subtitle?.value = if (viewModel.lastUpdateDate.value == null) {
            resources.getString(R.string.empty)
        } else {
            resources.getString(
                R.string.dashboard_risky_encounter_subtitle_ok,
                viewModel.lastUpdateDate.value,
                viewModel.lastUpdateTime.value
            )
        }

        riskyDashboardCard?.icon?.value =
            getDrawable(requireContext(), R.drawable.ic_no_risky_encounter)

    }

    private fun setSomeRiskyEncounters() {
        val riskyDashboardCard = viewModel.allCards[Type.RISKY_ENCOUNTER]
        val exposuresCount = viewModel.exposuresCount.value ?: 0
        L.i("some risky encounters are found: $exposuresCount")

        riskyDashboardCard?.title?.value = resources.getQuantityString(
            R.plurals.dashboard_risky_encounter_title_bad,
            exposuresCount,
            exposuresCount
        )

        riskyDashboardCard?.subtitle?.value = resources.getString(
            R.string.dashboard_risky_encounter_subtitle_bad,
            viewModel.lastExposureDate.value
        )

        riskyDashboardCard?.icon?.value =
            getDrawable(requireContext(), R.drawable.ic_risky_encounter)


    }

}
