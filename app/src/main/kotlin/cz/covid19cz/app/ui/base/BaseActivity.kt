package cz.covid19cz.app.ui.base

import androidx.databinding.ViewDataBinding
import arch.view.BaseArchActivity
import cz.covid19cz.app.R
import kotlin.reflect.KClass


open class BaseActivity<B : ViewDataBinding, VM : BaseVM>(layoutId: Int, viewModelClass: KClass<VM>) :
    BaseArchActivity<B, VM>(layoutId, viewModelClass) {
    override fun onBackPressed() {
        val childFragmentManager = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager

        if ((childFragmentManager?.fragments?.get(0) as? BaseFragment<*, *>)?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
}