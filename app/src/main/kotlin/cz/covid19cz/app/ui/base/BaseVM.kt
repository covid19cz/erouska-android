package cz.covid19cz.app.ui.base

import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.app.ext.execute
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

open class BaseVM : BaseArchViewModel() {

    private val disposables = CompositeDisposable()

    fun <T> subscribe(observable: Observable<T>, onError: (Throwable) -> Unit, onNext: (T) -> Unit) {
        disposables.add(observable.execute(onNext, onError))
    }

    fun <T> subscribe(single: Single<T>, onError: (Throwable) -> Unit, onNext: (T) -> Unit) {
        disposables.add(single.execute(onNext, onError))
    }

    fun <T> subscribe(maybe: Maybe<T>, onError: (Throwable) -> Unit, onSuccess: (T) -> Unit) {
        disposables.add(maybe.execute(onSuccess, onError))
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }
}