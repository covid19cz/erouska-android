package cz.covid19cz.erouska.ui.help

import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.navigation.NavOptions
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentBatteryOptimizationBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.BatteryOptimization
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.DeviceInfo

class BatteryOptimizationFragment :
    BaseFragment<FragmentBatteryOptimizationBinding, BatteryOptimizationVM>(
        R.layout.fragment_battery_optimization,
        BatteryOptimizationVM::class
    ) {

    private val url = BatteryOptimization.getTutorialLink()
    private val chromePackageName by lazy {
        if (url != null) {
            return@lazy CustomTabHelper.getPackageNameToUse(requireContext(), url)
        }
        null
    }
    private val customTabsConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(
            name: ComponentName,
            client: CustomTabsClient
        ) {
            client.warmup(0)
            val session = client.newSession(null)
            session?.mayLaunchUrl(Uri.parse(url), null, null)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

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
            showGuide()
        }
        preloadUrl()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.onboarding, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        activity?.unbindService(customTabsConnection)
        super.onDestroyView()
    }

    private fun preloadUrl() {
        if (url != null && chromePackageName != null) {
            CustomTabsClient.bindCustomTabsService(
                requireContext(),
                chromePackageName,
                customTabsConnection
            )
        }
    }

    private fun showGuide() {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setToolbarColor(resources.getColor(R.color.colorPrimary))
            .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_action_up))
            .build()
        if (url != null) {
            if (chromePackageName != null) {
                intent.launchUrl(requireContext(), Uri.parse(url))
            } else {
                // Custom Tabs not available
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }
}
