package cz.covid19cz.app.ui.base

import androidx.databinding.ViewDataBinding
import arch.view.BaseArchActivity
import kotlin.reflect.KClass


open class BaseActivity<B : ViewDataBinding, VM : BaseVM>(layoutId: Int, viewModelClass: KClass<VM>) :
    BaseArchActivity<B, VM>(layoutId, viewModelClass) {
}