package cz.covid19cz.app.ui.sandbox

import arch.view.BaseArchFragment
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentSandboxBinding

interface DashMainView

class SandboxFragment : BaseArchFragment<FragmentSandboxBinding, SandboxVM>(R.layout.fragment_sandbox, SandboxVM::class),
    DashMainView {

}