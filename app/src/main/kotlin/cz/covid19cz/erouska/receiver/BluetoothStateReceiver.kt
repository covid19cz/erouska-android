package cz.covid19cz.erouska.receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.covid19cz.erouska.service.CovidService

class BluetoothStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED == intent?.action) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_OFF -> CovidService.update(context)
                BluetoothAdapter.STATE_ON -> CovidService.update(context)
            }

        }
    }


}