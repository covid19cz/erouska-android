package cz.covid19cz.erouska.ui.sandbox

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSandboxConfigBinding
import cz.covid19cz.erouska.ui.base.BaseFragment

class SandboxConfigFragment : BaseFragment<FragmentSandboxConfigBinding, SandboxConfigVM>(
    R.layout.fragment_sandbox_config,
    SandboxConfigVM::class
) {

}
