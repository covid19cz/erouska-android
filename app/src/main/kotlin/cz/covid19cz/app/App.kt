package cz.covid19cz.app

import arch.BaseApp
import com.idescout.sql.SqlScoutServer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        setupKoin()
        SqlScoutServer.create(this, packageName);
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@App)
            modules(allModules)
        }
    }
}