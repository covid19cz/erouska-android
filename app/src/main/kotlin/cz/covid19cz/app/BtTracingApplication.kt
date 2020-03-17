package cz.covid19cz.app

import android.app.Application
import cz.covid19cz.app.di.appModule
import cz.covid19cz.app.di.coroutineContextModule
import cz.covid19cz.app.di.viewModelModule
import cz.covid19cz.app.utils.BtUtils
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.androidModule

class BtTracingApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidModule(this@BtTracingApplication))
        import(appModule)
        import(coroutineContextModule)
        import(viewModelModule)
    }

    override fun onCreate() {
        super.onCreate()
        BtUtils.init(this)
    }
}