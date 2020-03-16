package cz.covid19cz.app.di

import androidx.lifecycle.ViewModelProvider
import cz.covid19cz.app.utils.ViewModelFactory
import org.kodein.di.Kodein.Module
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

private const val MODULE_NAME = "Activity Module"

val activityModule = Module(MODULE_NAME, false) {
    bind<ViewModelProvider.Factory>() with singleton {
        ViewModelFactory(
            dkodein
        )
    }
}