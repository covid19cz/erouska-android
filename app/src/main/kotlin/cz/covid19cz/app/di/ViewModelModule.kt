package cz.covid19cz.app.di

import cz.covid19cz.app.ext.bindViewModel
import cz.covid19cz.app.ui.dash.DashViewModel
import org.kodein.di.Kodein.Module
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

private const val MODULE_NAME = "ViewModel Module"

val viewModelModule = Module(MODULE_NAME, false) {
    bindViewModel<DashViewModel>() with singleton {
        DashViewModel(
            instance()
        )
    }
}