package cz.covid19cz.erouska.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.service.CovidService

class ActionsReceiver : BroadcastReceiver() {
    companion object {
        private const val PREFIX = BuildConfig.APPLICATION_ID + ".EROUSKA"
        const val ACTION_PAUSE = "${PREFIX}_PAUSE"
        const val ACTION_RESUME = "${PREFIX}_RESUME"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_PAUSE -> {
                context.startService(CovidService.pause(context))
            }
            ACTION_RESUME -> {
                context.startService(CovidService.resume(context))
            }
        }
    }
}