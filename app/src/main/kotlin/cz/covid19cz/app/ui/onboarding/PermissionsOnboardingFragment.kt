package cz.covid19cz.app.ui.onboarding

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentPermissionsOnboardingBinding
import cz.covid19cz.app.ext.getLocationPermission
import cz.covid19cz.app.ext.openLocationSettings
import cz.covid19cz.app.ext.openPermissionsScreen
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.onboarding.event.PermissionsOnboarding
import io.reactivex.disposables.CompositeDisposable

class PermissionsOnboardingFragment :
    BaseFragment<FragmentPermissionsOnboardingBinding, PermissionsOnboardingVM>(
        R.layout.fragment_permissions_onboarding,
        PermissionsOnboardingVM::class
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
                PermissionsOnboarding.Command.ENABLE_LOCATION -> enableLocation()
                PermissionsOnboarding.Command.PERMISSION_REQUIRED -> showPermissionRequiredDialog()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.help, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkState()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun onBluetoothEnabled() {
        viewModel.onBluetoothEnabled()
    }

    private fun requestLocation() {
        compositeDisposable.add(rxPermissions
            .request(getLocationPermission())
            .subscribe { granted: Boolean ->
                if (granted) {
                    viewModel.onLocationPermissionGranted()
                } else {
                    viewModel.onLocationPermissionDenied()
                }
            })
    }

    private fun showPermissionRequiredDialog() {
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.permission_rationale_title))
            .setMessage(getString(R.string.permission_rationale_body))
            .setPositiveButton(getString(R.string.permission_rationale_settings))
            { dialog, which ->
                dialog.dismiss()
                requireContext().openPermissionsScreen()
            }
            .setNegativeButton(getString(R.string.permission_rationale_dismiss))
            { dialog, which -> dialog.dismiss() }
            .show()
    }

    private fun enableLocation() {
        requireContext().openLocationSettings()
    }

    private fun enableBluetooth() {
        requestEnableBt()
    }
}