package com.covid19cz.bt_tracing.di

import com.covid19cz.bt_tracing.ext.bindViewModel
import com.covid19cz.bt_tracing.ui.dash.DashViewModel
import org.kodein.di.Kodein.Module
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

private const val MODULE_NAME = "ViewModel Module"

val viewModelModule = Module(MODULE_NAME, false) {
    bindViewModel<DashViewModel>() with singleton { DashViewModel(instance()) }
}