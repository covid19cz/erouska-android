package cz.covid19cz.erouska.ui.sandbox.event

sealed class SandboxEvent {
    class KeyExportDownloadDone(val lastDownload: String, val filenames: List<String>) : SandboxEvent()
    class LastDownload(val name: String) : SandboxEvent()
}