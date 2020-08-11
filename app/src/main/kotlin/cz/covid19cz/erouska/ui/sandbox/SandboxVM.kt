package cz.covid19cz.erouska.ui.sandbox

import androidx.lifecycle.viewModelScope
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch

class SandboxVM(
    private val serverRepository: ExposureServerRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    fun downloadKeyExport() {
        viewModelScope.launch {
            val lastKeyExport = prefs.lastKeyExport()
            val result = serverRepository.downloadKeyExport(lastKeyExport)
            val lastDownload = result.lastDownload
            prefs.setLastKeyExport(lastDownload)

            L.d("files=${result.files}")
        }
    }

}