package cz.covid19cz.erouska.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.bt.BluetoothRepository
import cz.covid19cz.erouska.databinding.ActivityMainBinding
import cz.covid19cz.erouska.ext.hasLocationPermission
import cz.covid19cz.erouska.ext.isLocationEnabled
import cz.covid19cz.erouska.service.CovidService
import cz.covid19cz.erouska.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class MainActivity :
    BaseActivity<ActivityMainBinding, MainVM>(R.layout.activity_main, MainVM::class) {

    private val localBroadcastManager by inject<LocalBroadcastManager>()
    private val bluetoothRepository by inject<BluetoothRepository>()

    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    CovidService.ACTION_MASK_STARTED -> viewModel.serviceRunning.value = true
                    CovidService.ACTION_MASK_STOPPED -> viewModel.serviceRunning.value = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        registerServiceStateReceivers()
        findNavController(R.id.nav_host_fragment).let {
            bottom_navigation.setOnNavigationItemSelectedListener { item ->
                navigate(
                    item.itemId,
                    navOptions = NavOptions.Builder().setPopUpTo(R.id.nav_graph, false).build()
                )
                true
            }
            it.addOnDestinationChangedListener { _, destination, arguments ->
                updateTitle(destination)
                updateBottomNavigation(destination, arguments)
            }
        }
        viewModel.serviceRunning.observe(this, Observer { isRunning ->
            ContextCompat.getColor(
                this,
                if (isRunning && passesRequirements()) R.color.green else R.color.red
            ).let {
                bottom_navigation.getOrCreateBadge(R.id.nav_dashboard).backgroundColor = it
            }
        })

        viewModel.serviceRunning.value = CovidService.isRunning(this)
    }

    override fun onDestroy() {
        localBroadcastManager.unregisterReceiver(serviceStateReceiver)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_help -> {
                navigate(R.id.nav_help, Bundle().apply { putBoolean("fullscreen", true) })
                true
            }
            else -> {
                NavigationUI.onNavDestinationSelected(
                    item,
                    findNavController(R.id.nav_host_fragment)
                ) || super.onOptionsItemSelected(item)
            }
        }
    }

    private fun updateTitle(destination: NavDestination) {
        if (destination.label != null) {
            title = destination.label
        } else {
            setTitle(R.string.app_name)
        }
    }

    private fun updateBottomNavigation(
        destination: NavDestination,
        arguments: Bundle?
    ) {
        bottom_navigation.visibility =
            if (destination.arguments["fullscreen"]?.defaultValue == true
                || arguments?.getBoolean("fullscreen") == true
            ) {
                GONE
            } else {
                VISIBLE
            }
    }

    private fun registerServiceStateReceivers() {
        localBroadcastManager.registerReceiver(
            serviceStateReceiver,
            IntentFilter(CovidService.ACTION_MASK_STARTED)
        )
        localBroadcastManager.registerReceiver(
            serviceStateReceiver,
            IntentFilter(CovidService.ACTION_MASK_STOPPED)
        )
    }

    private fun passesRequirements(): Boolean {
        return bluetoothRepository.isBtEnabled() && isLocationEnabled() && hasLocationPermission()
    }

}
