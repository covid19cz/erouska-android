package cz.covid19cz.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.covid19cz.app.service.CovidService

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        intent?.action?.let { action ->
            when (action) {
                Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON -> CovidService.screenStateChange(
                    context,
                    action
                )
            }
        }
    }
}