package cz.covid19cz.app.ui.dash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.DashMainBinding
import cz.covid19cz.app.ext.withViewModel
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.login.LoginActivity

interface DashMainView

class DashFragment : BaseFragment(),
    DashMainView {
    lateinit var binding: DashMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dash_main, container, false)
        binding.lifecycleOwner = this
        binding.view = this

        activity?.withViewModel<DashViewModel>(viewModelFactory) {
            binding.viewModel = this
        }
        binding.login.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
        }

        return binding.root
    }
}