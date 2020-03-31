package cz.covid19cz.erouska.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import cz.covid19cz.erouska.service.CovidService
import cz.covid19cz.erouska.utils.L

class BatterSaverStateReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        L.d("Battery saver state changed")
        if (action.equals(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)) {
            CovidService.update(context)
        }
    }
}