package cz.covid19cz.erouska.ui.sandbox

import android.os.Bundle
import androidx.lifecycle.observe
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSandboxBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.sandbox.event.SandboxEvent
import kotlinx.android.synthetic.main.fragment_sandbox.*

class SandboxFragment :
    BaseFragment<FragmentSandboxBinding, SandboxVM>(R.layout.fragment_sandbox, SandboxVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.observe(this) { updateState(it) }
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


}