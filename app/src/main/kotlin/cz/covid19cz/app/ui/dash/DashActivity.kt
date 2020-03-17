package cz.covid19cz.app.ui.dash

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cz.covid19cz.app.BtTracingApplication
import cz.covid19cz.app.R.layout
import cz.covid19cz.app.R.style
import cz.covid19cz.app.ext.withViewModel
import cz.covid19cz.app.service.BtTracingService
import cz.covid19cz.app.ui.base.BaseActivity
import cz.covid19cz.app.utils.BtUtils
import cz.covid19cz.app.utils.Log
import org.kodein.di.LazyKodein
import org.kodein.di.erased.instance


class DashActivity : BaseActivity()  {

    companion object{
        const val REQUEST_BT_ENABLE = 1000
        const val REQUEST_PERMISSION_FINE_LOCATION = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_dash)

        withViewModel<DashViewModel>(viewModelFactory) {
            // TODO
        }
        startBtService()
    }

    fun startBtService(){
        if (BtUtils.hasBle(this)){
            if (!BtUtils.isBtEnabled()){
                BtUtils.enable()
            }
            if (!hasLocationPermissions()){
                requestLocationPermission()
            } else {
                BtTracingService.startService(this)
            }
        } else {
            // TODO: Device doesn't support BLE
        }
    }

    fun requestEnableBt(){
        fun requestBtEnable(c : Context){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE)
            Log.d( "Requested user enables Bluetooth. Try starting the scan again.")
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // TODO: Show an explanation to the user
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_FINE_LOCATION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                BtTracingService.startService(this)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}
