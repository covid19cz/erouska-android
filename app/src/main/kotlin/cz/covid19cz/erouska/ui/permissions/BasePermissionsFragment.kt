package cz.covid19cz.erouska.ui.permissions

import android.os.Bundle
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPermissionsOnboardingBinding
import cz.covid19cz.erouska.ext.openPermissionsScreen
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.permissions.onboarding.event.PermissionsOnboarding
import io.reactivex.disposables.CompositeDisposable
import kotlin.reflect.KClass


open class BasePermissionsFragment<T : BasePermissionsVM>(
    @LayoutRes layout: Int,
    viewModelClass: KClass<T>
) :
    BaseFragment<FragmentPermissionsOnboardingBinding, T>(
        layout,
        viewModelClass
    ) {

    private lateinit var rxPermissions: RxPermissions
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermissions = RxPermissions(this)

        subscribe(PermissionsOnboarding::class) {
            when (it.command) {
                PermissionsOnboarding.Command.ENABLE_BT -> enableBluetooth()
                PermissionsOnboarding.Command.REQUEST_LOCATION_PERMISSION -> requestLocation()
                PermissionsOnboarding.Command.PERMISSION_REQUIRED -> showPermissionRequiredDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkState()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun onBluetoothEnabled() {
        viewModel.onBluetoothEnabled()
    }

    // TODO: Remove
    private fun requestLocation() {
        viewModel.onLocationPermissionGranted()
    }

    private fun showPermissionRequiredDialog() {
        val layout = layoutInflater.inflate(R.layout.alert_dialog_title, null)
        layout.findViewById<TextView>(R.id.alert_title)?.let {
            it.text = getString(R.string.permission_rationale_title)
        }

        MaterialAlertDialogBuilder(context)
            .setCustomTitle(layout)
            .setMessage(getString(R.string.permission_rationale_body))
            .setPositiveButton(getString(R.string.permission_rationale_settings))
            { dialog, _ ->
                dialog.dismiss()
                requireContext().openPermissionsScreen()
            }
            .setNegativeButton(getString(R.string.permission_rationale_dismiss))
            { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun enableBluetooth() {
        requestEnableBt()
    }
}
