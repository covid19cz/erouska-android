package cz.covid19cz.erouska.ui.sandbox

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSandboxBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.sandbox.event.SandboxEvent
import kotlinx.android.synthetic.main.fragment_sandbox.*

class SandboxFragment :
    BaseFragment<FragmentSandboxBinding, SandboxVM>(R.layout.fragment_sandbox, SandboxVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(GmsApiErrorEvent::class){
            startIntentSenderForResult(it.status.resolution?.intentSender,
                REQUEST_GMS_ERROR_RESOLUTION, null, 0, 0, 0, null)
        }
        viewModel.state.observe(this, Observer { updateState(it) })
    }

    private fun updateState(state: SandboxEvent) {
        when (state) {
            is SandboxEvent.LastDownload -> sandbox_last_download.text = state.name
            is SandboxEvent.KeyExportDownloadDone -> {
                sandbox_last_download.text = state.lastDownload
                printDownload(state.filenames)
            }
        }
    }

    private fun printDownload(names: List<String>) {
        sandbox_new_files.text = if (names.isEmpty()) {
            "No new files"
        } else {
            names.joinToString("\n")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GMS_ERROR_RESOLUTION && resultCode == Activity.RESULT_OK) {
            viewModel.refreshTeks()
        }
    }

    companion object {
        const val REQUEST_GMS_ERROR_RESOLUTION = 42
    }
}
