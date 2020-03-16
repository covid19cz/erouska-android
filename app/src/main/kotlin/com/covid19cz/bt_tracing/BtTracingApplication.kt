package com.covid19cz.bt_tracing

import android.app.Application
import com.covid19cz.bt_tracing.di.appModule
import com.covid19cz.bt_tracing.di.coroutineContextModule
import com.covid19cz.bt_tracing.di.viewModelModule
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
}