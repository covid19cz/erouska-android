package cz.covid19cz.app.receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.covid19cz.app.utils.Log

class BluetoothStateReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.getAction()

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when(state) {
                BluetoothAdapter.STATE_OFF -> {
                    Log.d("Bluetooth OFF")
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {

                }
                BluetoothAdapter.STATE_ON -> {
                    Log.d("Bluetooth ON")
                }
                BluetoothAdapter.STATE_TURNING_ON -> {

                }
            }

        }
    }


}