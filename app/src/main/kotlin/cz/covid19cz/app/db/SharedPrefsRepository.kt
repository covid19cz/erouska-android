package cz.covid19cz.app.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SharedPrefsRepository(c : Context) {

    companion object{
        const val DEVICE_BUID = "DEVICE_BUID"
    }

    val prefs : SharedPreferences = c.getSharedPreferences("prefs", MODE_PRIVATE)

    fun putDeviceBuid(buid : String){
        prefs.edit().putString(DEVICE_BUID, buid).apply()
    }

    fun removeDeviceBuid(){
        prefs.edit().remove(DEVICE_BUID).apply()
    }

    fun getDeviceBuid() : String?{
        return prefs.getString(DEVICE_BUID, null)?.let {
            // check BUID and delete if old 10 chars BUID present
            if (it.length != 20){
                removeDeviceBuid()
                return@let null
            }
            return@let it
        }
    }

    private fun checkBuid(){

        if (prefs.contains(DEVICE_BUID) && getDeviceBuid()?.length != 20){

        }
    }

    fun clear(){
        prefs.edit().clear().apply()
    }
}