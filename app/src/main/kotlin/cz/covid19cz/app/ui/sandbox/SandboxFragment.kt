package cz.covid19cz.app.ui.sandbox

import android.content.Intent
import android.os.Bundle
import android.view.View
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentSandboxBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_sandbox.*

interface DashMainView

class SandboxFragment : BaseFragment<FragmentSandboxBinding, SandboxVM>(R.layout.fragment_sandbox, SandboxVM::class),
    DashMainView {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vLogin.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
        }
    }

}