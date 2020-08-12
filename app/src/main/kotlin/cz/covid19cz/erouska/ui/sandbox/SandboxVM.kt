package cz.covid19cz.erouska.ui.sandbox

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.sandbox.event.SandboxEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch

class SandboxVM(
    private val serverRepository: ExposureServerRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val state = MutableLiveData<SandboxEvent>()

    init {
        state.value = SandboxEvent.LastDownload(prefs.lastKeyExportFileName())
    }

    fun downloadKeyExport() {
        viewModelScope.launch {
            val files = serverRepository.downloadKeyExport()

            val lastDownload = prefs.lastKeyExportFileName()

            state.value = SandboxEvent.KeyExportDownloadDone(lastDownload, files.map { it.path.split('/').takeLast(2).joinToString("/") })

            L.d("files=${files}")
        }
    }

}