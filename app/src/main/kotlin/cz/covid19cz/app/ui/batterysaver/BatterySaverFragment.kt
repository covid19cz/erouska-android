package cz.covid19cz.app.ui.batterysaver

import android.content.ComponentName
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import android.view.View
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentBatterySaverBinding
import cz.covid19cz.app.ext.isBatterySaverEnabled
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.batterysaver.event.BatterSaverCommandEvent
import cz.covid19cz.app.ui.sandbox.SandboxFragment
import cz.covid19cz.app.ui.sandbox.SandboxFragment.Companion

class BatterySaverFragment : BaseFragment<FragmentBatterySaverBinding, BatterySaverVM>(
    R.layout.fragment_battery_saver,
    BatterySaverVM::class
) {

    companion object {
        const val REQUEST_BATTERY_SAVER_SETTINGS = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(BatterSaverCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                BatterSaverCommandEvent.Command.DISABLE_BATTER_SAVER -> disableBatterySaver()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_BATTERY_SAVER_SETTINGS -> {
                if (!requireContext().isBatterySaverEnabled()) {
                    navigate(R.id.nav_dashboard)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)
    }

    fun disableBatterySaver() {
        val batterySaverIntent = if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        } else {
            val intent = Intent()
            intent.component = ComponentName(
                "com.android.settings",
                "com.android.settings.Settings\$BatterySaverSettingsActivity"
            )
            intent
        }
        startActivityForResult(batterySaverIntent, REQUEST_BATTERY_SAVER_SETTINGS)
    }
}