package cz.covid19cz.app.ui.sandbox

import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentSandboxBinding
import cz.covid19cz.app.ui.base.BaseFragment

interface DashMainView

class SandboxFragment : BaseFragment<FragmentSandboxBinding, SandboxVM>(R.layout.fragment_sandbox, SandboxVM::class),
    DashMainView {

}