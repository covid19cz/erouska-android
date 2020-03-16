package cz.covid19cz.app.di

import org.kodein.di.Kodein.Module

private const val MODULE_NAME = "Repository Module"

val repositoryModule = Module(MODULE_NAME, false) {
    //bind<XYZRepository>() with singleton { XYZRepositoryImpl(instance()) }
}