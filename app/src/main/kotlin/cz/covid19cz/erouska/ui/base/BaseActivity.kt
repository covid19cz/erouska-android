package cz.covid19cz.erouska.ui.base

import android.app.Activity
import android.content.Intent
import androidx.databinding.ViewDataBinding
import arch.view.BaseArchActivity
import com.google.android.play.core.install.model.AppUpdateType
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.exposurenotifications.InAppUpdateHelper
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

        forceUpdateIfNeeded()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == InAppUpdateHelper.APP_FORCE_UPDATE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                forceUpdateIfNeeded()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun forceUpdateIfNeeded() {
        if (InAppUpdateHelper.isObsolete()) {
            InAppUpdateHelper.checkForAppUpdateAndUpdate(this, AppUpdateType.IMMEDIATE)
        }
    }
}