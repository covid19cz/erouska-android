package cz.covid19cz.app.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cz.covid19cz.app.di.fragmentModule
import org.kodein.di.Copy
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinContext
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import org.kodein.di.generic.kcontext

abstract class BaseFragment : Fragment(), KodeinAware {
    override val kodeinContext: KodeinContext<*> get() = kcontext(activity)
    private val _parentKodein by closestKodein()

    override val kodein: Kodein = Kodein.lazy {
        extend(_parentKodein, copy = Copy.All)
        import(fragmentModule)
    }

    protected val viewModelFactory: ViewModelProvider.Factory by instance()

}