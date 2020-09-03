package cz.covid19cz.erouska.ui.sandbox

import android.os.Bundle
import android.view.View
import arch.adapter.RecyclerLayoutStrategy
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSandboxDataBinding
import cz.covid19cz.erouska.ui.base.BaseFragment

class SandboxDataFragment : BaseFragment<FragmentSandboxDataBinding, SandboxDataVM>(
    R.layout.fragment_sandbox_data,
    SandboxDataVM::class
) {
    private val exposureWindowsLayoutStrategy = object : RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return when (item) {
                is ExposureWindow -> R.layout.item_exposure_window
                is String -> R.layout.item_scan_instance_header
                is ScanInstance -> R.layout.item_scan_instance
                else -> throw RuntimeException("Layout mapping not found")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.exposureWindowsLayoutStrategy = exposureWindowsLayoutStrategy
    }
}
