package cz.covid19cz.app.ext

import androidx.lifecycle.ViewModel
import org.kodein.di.Kodein
import org.kodein.di.generic.bind

inline fun <reified T : ViewModel> Kodein.Builder.bindViewModel(overrides: Boolean? = null): Kodein.Builder.TypeBinder<T> {
    return bind<T>(T::class.java.simpleName, overrides)
}