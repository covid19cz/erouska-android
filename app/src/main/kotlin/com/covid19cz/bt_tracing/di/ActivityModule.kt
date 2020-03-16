package com.covid19cz.bt_tracing.di

import androidx.lifecycle.ViewModelProvider
import com.covid19cz.bt_tracing.utils.ViewModelFactory
import org.kodein.di.Kodein.Module
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

private const val MODULE_NAME = "Activity Module"

val activityModule = Module(MODULE_NAME, false) {
    bind<ViewModelProvider.Factory>() with singleton { ViewModelFactory(dkodein) }
}