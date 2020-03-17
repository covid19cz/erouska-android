package cz.covid19cz.app

import cz.covid19cz.app.ui.main.MainVM
import cz.covid19cz.app.ui.sandbox.SandboxVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainVM() }
    viewModel { SandboxVM() }
}

val allModules = listOf(viewModelModule)
