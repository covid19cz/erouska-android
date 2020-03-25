package cz.covid19cz.app.ui.confirm

import androidx.lifecycle.viewModelScope
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.confirm.event.ErrorEvent
import cz.covid19cz.app.ui.confirm.event.FinishedEvent
import cz.covid19cz.app.utils.Auth
import cz.covid19cz.app.utils.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ConfirmationVM(
    private val database: DatabaseRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    private val functions = Firebase.functions("europe-west2")

    fun deleteAllData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val data = hashMapOf(
                        "buid" to prefs.getDeviceBuid()
                    )
                    functions.getHttpsCallable("deleteUploads").call(data).await()
                    database.clear()
                    publish(FinishedEvent())
                } catch (e: Exception) {
                    L.e(e)
                    publish(ErrorEvent(e))
                }
            }
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    functions.getHttpsCallable("deleteUser").call().await()
                    database.clear()
                    prefs.clear()
                    Auth.signOut()
                    publish(FinishedEvent())
                } catch (e: Exception) {
                    L.e(e)
                    publish(ErrorEvent(e))
                }
            }
        }
    }

}