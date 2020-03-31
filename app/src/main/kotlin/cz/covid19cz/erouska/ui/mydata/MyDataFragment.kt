package cz.covid19cz.erouska.ui.mydata

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
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
        subscribe(ShowDescriptionEvent::class) {
            showMessageDialog(getString(R.string.my_data_description, AppConfig.persistDataDays))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(false)
        setupTabs()
    }

    private fun setupTabs(){
        viewPager.adapter = MyDataPagerAdapter(requireContext())
        tabs.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.my_data, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showMessageDialog(message: String) {
        MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setPositiveButton(getString(android.R.string.ok))
            { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
