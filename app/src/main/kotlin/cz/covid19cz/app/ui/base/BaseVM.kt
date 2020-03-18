package cz.covid19cz.app.ui.base

import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.app.ext.execute
import cz.covid19cz.app.repository.device.DeviceRepository
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseVM : BaseArchViewModel() {

    private val disposables = CompositeDisposable()

    fun <T> subscribe(observable: Observable<T>, onError: (Throwable) -> Unit, onNext: (T) -> Unit) : Disposable {
        val disposable = observable.execute(onNext, onError)
        disposables.add(disposable)
        return disposable
    }

    fun <T> subscribe(single: Single<T>, onError: (Throwable) -> Unit, onNext: (T) -> Unit) : Disposable {
        val disposable = single.execute(onNext, onError)
        disposables.add(disposable)
        return disposable
    }

    fun <T> subscribe(maybe: Maybe<T>, onError: (Throwable) -> Unit, onSuccess: (T) -> Unit) : Disposable {
        val disposable = maybe.execute(onSuccess, onError)
        disposables.add(disposable)
        return disposable
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }
}