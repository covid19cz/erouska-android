package cz.covid19cz.erouska.ui.sandbox

import androidx.lifecycle.viewModelScope
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch

class SandboxVM(
    private val serverRepository: ExposureServerRepository,
    prefs : SharedPrefsRepository
) : BaseVM() {

    fun downloadKeyExport() {
        viewModelScope.launch {
            val files = serverRepository.downloadKeyExport()
            L.d("files=$files")
        }
    }

}