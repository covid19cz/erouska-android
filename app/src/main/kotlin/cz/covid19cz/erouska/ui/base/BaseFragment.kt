package cz.covid19cz.erouska.ui.base

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import androidx.databinding.ViewDataBinding
import arch.view.BaseArchFragment
import arch.viewmodel.BaseArchViewModel
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.utils.L
import kotlin.reflect.KClass


abstract class BaseFragment<B : ViewDataBinding, VM : BaseArchViewModel>(
    layoutId: Int,
    viewModelClass: KClass<VM>
) : BaseArchFragment<B, VM>(layoutId, viewModelClass) {

    companion object {
        const val REQUEST_BT_ENABLE = 1000
    }

    var snackBar: Snackbar? = null

    protected open fun showSnackBar(@StringRes stringRes: Int) {
        showSnackBar(getString(stringRes))
    }

    protected open fun showSnackBarForever(@StringRes stringRes: Int) {
        showSnackBarForever(getString(stringRes))
    }

    protected open fun showSnackBar(@StringRes stringRes: Int, vararg args: Any) {
        showSnackBar(getString(stringRes, args))
    }

    protected open fun showSnackBar(text: String) {
        showSnackBarWithDuration(text, Snackbar.LENGTH_LONG)
    }

    protected open fun showSnackBarForever(text: String) {
        showSnackBarWithDuration(text, Snackbar.LENGTH_INDEFINITE)
    }

    protected open fun hideSnackBar() {
        snackBar?.dismiss()
        snackBar = null
    }

    private fun showSnackBarWithDuration(text: String, length: Int) {
        view?.let {
            if (snackBar == null) {
                snackBar = Snackbar.make(it, text, length)
            } else {
                snackBar?.setText(text)
            }
            snackBar?.show()
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

    fun enableUpInToolbar(enable: Boolean, iconType: IconType = IconType.UP) {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(enable)
        if (enable) {
            when (iconType) {
                IconType.CLOSE -> R.drawable.ic_action_close
                IconType.UP -> R.drawable.ic_action_up
            }.run {
                (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(this)
            }
        }
    }

    fun setTitle(title: String) {
        (activity as AppCompatActivity).supportActionBar?.title = title
    }

    fun requestEnableBt() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE)
    }

    fun requestLocationEnable() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    fun isPlayServicesObsolete(): Boolean {
        return try {
            val current = PackageInfoCompat.getLongVersionCode(
                requireContext().packageManager.getPackageInfo(
                    GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                    0
                )
            )

            current < AppConfig.minGmsVersionCode
        } catch (e: Exception) {
            L.e(e)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_BT_ENABLE -> {
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

    enum class IconType {
        UP, CLOSE
    }
}