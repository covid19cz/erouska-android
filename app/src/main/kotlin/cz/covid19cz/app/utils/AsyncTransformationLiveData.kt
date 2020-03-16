package cz.covid19cz.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AsyncTransformationLiveData<in T, R>(source: LiveData<T>,
    coroutineContext: CoroutineContext,
    transformation: (T) -> R) : MediatorLiveData<R>() {
    init {
        addSource(source) {
            GlobalScope.launch(coroutineContext) {
                if (it == null) {
                    postValue(null)
                } else {
                    postValue(transformation(it))
                }
            }
        }
    }
}