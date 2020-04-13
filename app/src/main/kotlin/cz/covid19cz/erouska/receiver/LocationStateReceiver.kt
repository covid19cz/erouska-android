package cz.covid19cz.erouska.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import cz.covid19cz.erouska.ext.batterySaverRestrictsLocation
import cz.covid19cz.erouska.service.CovidService
import cz.covid19cz.erouska.utils.L
import org.koin.core.KoinComponent
import org.koin.core.inject

class LocationStateReceiver : BroadcastReceiver(), KoinComponent {

    private val powerManager by inject<PowerManager>()

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        L.d("Location state changed")
        if (action.equals("android.location.PROVIDERS_CHANGED")
            && !powerManager.batterySaverRestrictsLocation()
        ) {
            CovidService.update(context)
        }
    }
}