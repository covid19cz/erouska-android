package cz.covid19cz.app.ui.confirm

import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import cz.covid19cz.app.db.DatabaseRepository
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.confirm.event.ErrorEvent
import cz.covid19cz.app.ui.confirm.event.FinishedEvent
import cz.covid19cz.app.utils.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ConfirmationVM(
    private val database: DatabaseRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    private val functions = Firebase.functions("europe-west2")

    fun deleteAllData() {
        runBlocking {
            withContext(Dispatchers.IO) {
                database.clear()
                val data = hashMapOf(
                    "buid" to prefs.getDeviceBuid()
                )
                functions.getHttpsCallable("deleteUploads").call(data).addOnSuccessListener {
                    publish(FinishedEvent())
                }.addOnFailureListener {
                    L.e(it)
                    publish(ErrorEvent(it))
                }
            }
        }
    }

}