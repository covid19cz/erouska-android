package cz.covid19cz.erouska

import arch.BaseApp
import com.facebook.stetho.Stetho
import com.idescout.sql.SqlScoutServer
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin

class App : BaseApp(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        setupKoin()
        // SQLScout - Database viewer for Android Studio
        SqlScoutServer.create(this, packageName)
        AppConfig.fetchRemoteConfig()
        AndroidThreeTen.init(this);

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@App)
            modules(allModules)
        }
    }
}
