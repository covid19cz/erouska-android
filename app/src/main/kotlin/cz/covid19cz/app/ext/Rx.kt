package cz.covid19cz.app.ext

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.execute(onNext : (t :T) -> Unit, onError : (t :Throwable) -> Unit): Disposable {
    return inBackground().subscribe(onNext, onError)
}

fun <T> Single<T>.execute(onNext : (t :T) -> Unit, onError : (t :Throwable) -> Unit): Disposable {
    return inBackground().subscribe(onNext, onError)
}

fun <T> Maybe<T>.execute(onSuccess : (t :T) -> Unit, onError : (t :Throwable) -> Unit): Disposable {
    return inBackground().subscribe(onSuccess, onError)
}

fun <T> Observable<T>.inBackground(): Observable<T> {
    return subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> Single<T>.inBackground(): Single<T> {
    return subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> Maybe<T>.inBackground(): Maybe<T> {
    return subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}