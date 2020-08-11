package cz.covid19cz.erouska.net.model

import java.io.File

data class KeyExportResult(
    val lastDownload: String,
    val files: List<File>
)