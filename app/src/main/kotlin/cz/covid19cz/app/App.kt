package cz.covid19cz.app

import android.app.Application
import arch.BaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        setupKoin()
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@App)
            modules(allModules)
        }
    }
}