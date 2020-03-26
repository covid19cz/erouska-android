package cz.covid19cz.app.ui.mydata

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentMyDataBinding
import cz.covid19cz.app.ui.base.BaseFragment
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
    }

    fun setupTabs(){
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