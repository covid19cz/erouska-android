package com.covid19cz.bt_tracing.ui.base

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.covid19cz.bt_tracing.di.repositoryModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.kcontext

abstract class BaseViewModel constructor(app: Application) : ViewModel(), KodeinAware, LifecycleObserver {
    override val kodeinContext = kcontext(app)
    private val _parentKodein by closestKodein(app)

    override val kodein: Kodein = Kodein.lazy {
        extend(_parentKodein)
        import(repositoryModule)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("BaseViewModel", "unsubscribeFromDataStore(): ")
    }
}