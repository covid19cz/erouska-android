package cz.covid19cz.app.ui.base

import androidx.databinding.ViewDataBinding
import arch.view.BaseArchFragment
import arch.viewmodel.BaseArchViewModel
import kotlin.reflect.KClass


abstract class BaseFragment<B : ViewDataBinding, VM : BaseArchViewModel>(layoutId: Int, viewModelClass: KClass<VM>) :
    BaseArchFragment<B, VM>(layoutId, viewModelClass) {

}