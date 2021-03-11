package cz.covid19cz.erouska.ui.activation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentActivationNotificationsBinding
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsErrorHandling
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.BluetoothDisabledEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActivationNotificationsFragment :
    BaseFragment<FragmentActivationNotificationsBinding, ActivationNotificationsVM>(
        R.layout.fragment_activation_notifications,
        ActivationNotificationsVM::class
    ) {

    companion object{
        private const val SCREEN_NAME = "Activation Notifications"
    }

    @Inject
    internal lateinit var exposureNotificationsErrorHandling: ExposureNotificationsErrorHandling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(NotificationsVerifiedEvent::class) {
            toEfgs()
        }

        subscribe(GmsApiErrorEvent::class) {
            exposureNotificationsErrorHandling.handle(it, this, SCREEN_NAME)
        }

        subscribe(BluetoothDisabledEvent::class) {
            requestEnableBt()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ExposureNotificationsErrorHandling.REQUEST_GMS_ERROR_RESOLUTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.enableNotifications()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.onboarding, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun toEfgs() {
        navigate(ActivationNotificationsFragmentDirections.actionNavActivationNotificationsToEfgsUpdate(onboarding = true, fullscreen = true))
    }

}