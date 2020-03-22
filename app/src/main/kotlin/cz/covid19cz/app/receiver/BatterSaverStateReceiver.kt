package cz.covid19cz.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import cz.covid19cz.app.service.CovidService
import cz.covid19cz.app.utils.Log

class BatterSaverStateReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d("Battery saver state changed")
        if (action.equals(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)) {
            CovidService.update(context)
        }
    }
}