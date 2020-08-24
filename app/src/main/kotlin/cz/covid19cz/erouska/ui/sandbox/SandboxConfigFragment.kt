package cz.covid19cz.erouska.ui.sandbox

import android.os.Bundle
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSandboxConfigBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.sandbox.event.SnackbarEvent

class SandboxConfigFragment : BaseFragment<FragmentSandboxConfigBinding, SandboxConfigVM>(
    R.layout.fragment_sandbox_config,
    SandboxConfigVM::class
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribe(SnackbarEvent::class) {
            showSnackBar(it.text)
        }
    }
}
