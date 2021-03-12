package cz.covid19cz.erouska.net.model

import cz.covid19cz.erouska.utils.L
import java.io.File

data class DownloadedKeys(
    val indexUrl: String,
    val files : List<File>,
    val urls : List<String>
) {
    fun getLastUrl() : String{
        return urls.last()
    }

    fun isValid(): Boolean {
        if (files.size != urls.size){
            L.w("Inconsistent download (${files.size}/${urls.size})")
        }
        return files.size == urls.size
    }
}