package cz.covid19cz.app

import arch.BaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.idescout.sql.SqlScoutServer
import cz.covid19cz.app.utils.Log
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        setupKoin()
        // SQLScout - Database viewer for Android Studio
        SqlScoutServer.create(this, packageName)
        AppConfig.fetchRemoteConfig()
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@App)
            modules(allModules)
        }
    }
}