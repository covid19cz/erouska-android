package cz.covid19cz.erouska.ui.activation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentActivationNotificationsBinding
import cz.covid19cz.erouska.ext.resolveUnknownGmsError
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.DashboardFragment
import cz.covid19cz.erouska.ui.dashboard.event.BluetoothDisabledEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.L

class ActivationNotificationsFragment :
    BaseFragment<FragmentActivationNotificationsBinding, ActivationNotificationsVM>(
        R.layout.fragment_activation_notifications,
        ActivationNotificationsVM::class
    ) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(NotificationsVerifiedEvent::class) {
            toActivation()
        }

        subscribe(GmsApiErrorEvent::class) {
            try {
                startIntentSenderForResult(
                    it.status.resolution?.intentSender,
                    DashboardFragment.REQUEST_GMS_ERROR_RESOLUTION,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (t : Throwable){
                L.e(t)
                it.status.resolveUnknownGmsError(requireContext())
            }
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
            DashboardFragment.REQUEST_GMS_ERROR_RESOLUTION -> {
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

    private fun toActivation() {
        navigate(R.id.action_nav_activation_notifications_to_activation_fragment)
    }

}