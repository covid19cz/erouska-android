package cz.covid19cz.erouska.file

import android.content.Context
import java.io.File

class FileManager(private val context: Context) {

    fun removeObsoleteData() {
        val obsoleteDb = File(context.filesDir.parent + "/databases/android-devices.db")
        if (obsoleteDb.exists()) {
            obsoleteDb.delete()
        }
    }

}
