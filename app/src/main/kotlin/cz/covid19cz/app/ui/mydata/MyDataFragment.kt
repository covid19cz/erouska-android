package cz.covid19cz.app.ui.mydata

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentMyDataBinding
import cz.covid19cz.app.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_my_data.*
import kotlinx.android.synthetic.main.fragment_my_data.view.*

class MyDataFragment :
    BaseFragment<FragmentMyDataBinding, MyDataVM>(R.layout.fragment_my_data, MyDataVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(ExportEvent.Complete::class) { _ ->
            showMessageDialog(getString(R.string.upload_successful))
        }
        subscribe(ExportEvent.Error::class) { event ->
            showMessageDialog(getString(R.string.upload_failed, event.message))
        }
        subscribe(ExportEvent.PleaseWait::class) {
            showMessageDialog(getString(R.string.please_wait_upload, it.minutes))
        }
        subscribe(ExportEvent.Confirmation::class) {
            MaterialAlertDialogBuilder(context)
                .setMessage(getString(R.string.upload_confirmation))
                .setPositiveButton(getString(R.string.send_data))
                { dialog, _ ->
                    dialog.dismiss()
                    viewModel.confirmSendingData()
                }
                .setNegativeButton(getString(R.string.permission_rationale_dismiss))
                { dialog, _ -> dialog.dismiss() }
                .show()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
    }

    fun setupTabs(){
        viewPager.adapter = MyDataPagerAdapter(requireContext())
        tabs.setupWithViewPager(viewPager)
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