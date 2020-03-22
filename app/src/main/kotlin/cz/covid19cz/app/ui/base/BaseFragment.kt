package cz.covid19cz.app.ui.base

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import arch.view.BaseArchFragment
import arch.viewmodel.BaseArchViewModel
import com.google.android.material.snackbar.Snackbar
import cz.covid19cz.app.ui.sandbox.SandboxFragment
import cz.covid19cz.app.ui.sandbox.SandboxFragment.Companion
import cz.covid19cz.app.utils.Log
import kotlin.reflect.KClass


abstract class BaseFragment<B : ViewDataBinding, VM : BaseArchViewModel>(layoutId: Int, viewModelClass: KClass<VM>) :
    BaseArchFragment<B, VM>(layoutId, viewModelClass) {

    protected open fun showSnackBar(@StringRes stringRes : Int) {
        showSnackBar(getString(stringRes))
    }

    protected open fun showSnackBar(@StringRes stringRes : Int, vararg args : Any) {
        showSnackBar(getString(stringRes, args))
    }

    protected open fun showSnackBar(text : String) {
        view?.let {
            Snackbar.make(it, text, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                return navController().navigateUp()
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    open fun onBluetoothEnabled() {
        // stub
    }

    fun enableUpInToolbar(enable: Boolean) {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(enable)
    }

    fun requestEnableBt() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, SandboxFragment.REQUEST_BT_ENABLE)
    }

    fun requestLocationEnable(){
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Companion.REQUEST_BT_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    onBluetoothEnabled()
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    open fun onBackPressed(): Boolean {
        return false
    }
}