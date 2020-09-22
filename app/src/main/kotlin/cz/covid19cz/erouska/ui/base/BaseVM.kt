package cz.covid19cz.erouska.ui.base

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavOptions
import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.ext.execute
import cz.covid19cz.erouska.utils.L
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.lang.IllegalStateException

open class BaseVM : BaseArchViewModel() {

    private val disposables = CompositeDisposable()

    fun <T> subscribe(observable: Observable<T>, onError: (Throwable) -> Unit, onNext: (T) -> Unit) : Disposable {
        val disposable = observable.execute(onNext, onError)
        disposables.add(disposable)
        return disposable
    }

    fun <T> subscribe(observable: Flowable<T>, onError: (Throwable) -> Unit, onNext: (T) -> Unit) : Disposable {
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

    override fun safeNavigate(@IdRes resId: Int, @IdRes currentDestination: Int, args: Bundle?, navOptions: NavOptions?) {
        try {
            super.safeNavigate(resId, currentDestination, args, navOptions)
        } catch (e: IllegalStateException) {
            // ignore navigation
            L.e(e)
        }
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }
}