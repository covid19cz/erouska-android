package cz.covid19cz.erouska.ui.sandbox

import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSandboxDataBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.sandbox.SandboxDataVM

class SandboxDataFragment : BaseFragment<FragmentSandboxDataBinding, SandboxDataVM>(
    R.layout.fragment_sandbox_data,
    SandboxDataVM::class
) {

}
