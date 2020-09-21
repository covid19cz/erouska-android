package cz.covid19cz.erouska.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPermissionssDisabledBinding
import cz.covid19cz.erouska.exposurenotifications.LocalNotificationsHelper
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.resolveUnknownGmsError
import cz.covid19cz.erouska.ext.shareApp
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.main.MainVM
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard.*

@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentPermissionssDisabledBinding, DashboardVM>(
    R.layout.fragment_dashboard,
    DashboardVM::class
) {

    companion object {
        const val REQUEST_GMS_ERROR_RESOLUTION = 42
    }

    private val mainViewModel: MainVM by viewModels()

    private val compositeDisposable = CompositeDisposable()
    private lateinit var rxPermissions: RxPermissions
    private var demoMode = false

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
            navigate(DashboardFragmentDirections.actionNavDashboardToNavExposures(demo = demoMode))
        }
        exposure_notification_close.setOnClickListener {
            viewModel.acceptLastExposure()
            exposure_notification_container.hide()
        }
        exposure_notification_more_info.setOnClickListener { navigate(DashboardFragmentDirections.actionNavDashboardToNavExposures(demo = demoMode)) }
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
        navigate(R.id.action_nav_dashboard_to_nav_permission_disabled)
    }

    private fun showWelcomeScreen() {
        navigate(R.id.action_nav_dashboard_to_nav_welcome_fragment)
    }

    private fun showPlayServicesUpdate() {
        navigate(R.id.action_nav_dashboard_to_nav_play_services_update)
    }

}
