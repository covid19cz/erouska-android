package cz.covid19cz.erouska.ui.help

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.navigation.NavOptions
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentBatteryOptimizationBinding
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.BatteryOptimization
import cz.covid19cz.erouska.utils.DeviceInfo

class BatteryOptimizationFragment :
    BaseFragment<FragmentBatteryOptimizationBinding, BatteryOptimizationVM>(
        R.layout.fragment_battery_optimization,
        BatteryOptimizationVM::class
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)
        activity?.setTitle(R.string.battery_opt_label)
        binding.doneBtn.setOnClickListener {
            navigate(
                R.id.action_batteryOptimizationFragment_to_nav_dashboard, null,
                NavOptions.Builder()
                    .setPopUpTo(
                        R.id.nav_graph,
                        true
                    ).build()
            )
        }
        binding.guideBtn.text = getString(R.string.guide_for, DeviceInfo.getManufacturer())
        binding.guideBtn.setOnClickListener {
            BatteryOptimization.getTutorialLink()?.let { it1 -> showWeb(it1) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.onboarding, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}
