package cz.covid19cz.erouska.ui.mydata

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import arch.adapter.RecyclerLayoutStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentMyDataBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_my_data.*

class MyDataFragment :
    BaseFragment<FragmentMyDataBinding, MyDataVM>(R.layout.fragment_my_data, MyDataVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(ExportEvent.PleaseWait::class) {
            showMessageDialog(getString(R.string.please_wait_upload, it.minutes))
        }
        subscribe(ExportEvent.Confirmation::class) {
            navigate(R.id.action_nav_my_data_to_nav_send_data)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)
        setupTabs()

        data_collection_info.setOnClickListener {
            navigate(MyDataFragmentDirections.actionNavMyDataToNavHelp(type = "data_collection"))
        }
    }

    private fun setupTabs(){
        viewPager.adapter = MyDataPagerAdapter(requireContext())
        tabs.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.my_data, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.data_description) {
            showMessageDialog(getString(R.string.my_data_description, AppConfig.persistDataDays))
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showMessageDialog(message: String) {
        MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setPositiveButton(getString(R.string.confirmation_button_close))
            { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    class MyDataLayoutStrategy: RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return when (item) {
                is MyDataVM.BatteryOptimizationFooter -> R.layout.my_data_battery_optimization
                else -> R.layout.item_my_data
            }
        }
    }
}
