package cz.covid19cz.app.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.DKodein
import org.kodein.di.generic.instanceOrNull

class ViewModelFactory constructor(
    private val injector: DKodein
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val instance = injector.instanceOrNull<ViewModel>(tag = modelClass.simpleName)
            ?: throw IllegalArgumentException("unknown model class " + modelClass)

        return try {
            instance as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}