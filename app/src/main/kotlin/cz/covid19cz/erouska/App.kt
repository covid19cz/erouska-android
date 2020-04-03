package cz.covid19cz.erouska

import arch.BaseApp
import com.idescout.sql.SqlScoutServer
import cz.covid19cz.erouska.db.AppDatabase
import cz.covid19cz.erouska.utils.L
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import java.io.File


class App : BaseApp(), KoinComponent{

    override fun onCreate() {
        super.onCreate()
        setupKoin()
        // SQLScout - Database viewer for Android Studio
        SqlScoutServer.create(this, packageName)
        AppConfig.fetchRemoteConfig()
        if (BuildConfig.DEBUG) {
            getDatabaseSize()
        }
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@App)
            modules(allModules)
        }
    }

    private fun getDatabaseSize(){
        val path: String = getDatabasePath(AppDatabase.DATABASE_NAME).toString()

        val file = File(path)
        val length: Long = file.length() // File size
        L.d("Database size: ${length/1024} kB")
    }
}