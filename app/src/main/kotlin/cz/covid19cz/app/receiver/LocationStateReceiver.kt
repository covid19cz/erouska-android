package cz.covid19cz.app.receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.covid19cz.app.utils.Log

class LocationStateReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.getAction()
        Log.d("Location state changed")

        if (action.equals("android.location.PROVIDERS_CHANGED")) {

        }
    }


}