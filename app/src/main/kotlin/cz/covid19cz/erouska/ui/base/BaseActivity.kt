package cz.covid19cz.erouska.ui.base

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.databinding.ViewDataBinding
import arch.view.BaseArchActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.utils.L
import kotlin.reflect.KClass


open class BaseActivity<B : ViewDataBinding, VM : BaseVM>(
    layoutId: Int,
    viewModelClass: KClass<VM>
) :
    BaseArchActivity<B, VM>(layoutId, viewModelClass) {
    override fun onBackPressed() {
        val childFragmentManager =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager

        if ((childFragmentManager?.fragments?.get(0) as? BaseFragment<*, *>)?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        updateIfNeeded()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                updateIfNeeded()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateIfNeeded() {
        if (isObsolete()) {
            checkForAppUpdate()
        }
    }

    private fun isObsolete(): Boolean {
        return BuildConfig.VERSION_CODE < AppConfig.minSupportedVersionCodeAndroid
    }

    private fun checkForAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        APP_UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    L.e(e)
                }
            }
        }
    }

    companion object {
        const val APP_UPDATE_REQUEST_CODE = 1777
    }
}