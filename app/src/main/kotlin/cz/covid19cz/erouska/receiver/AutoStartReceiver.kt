package cz.covid19cz.erouska.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import cz.covid19cz.erouska.db.SharedPrefsRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class AutoStartReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
       //TODO: Implement if start on boot will be required feature, otherwise delete
    }
}