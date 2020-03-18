package cz.covid19cz.app.ui.base

import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import arch.view.BaseArchFragment
import arch.viewmodel.BaseArchViewModel
import com.google.android.material.snackbar.Snackbar
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
}