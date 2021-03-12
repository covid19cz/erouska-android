package cz.covid19cz.erouska.ui.efgs

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentEfgsBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.efgs.event.EfgsCommandEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_efgs.*

@AndroidEntryPoint
class EfgsFragment :
    BaseFragment<FragmentEfgsBinding, EfgsVM>(R.layout.fragment_efgs, EfgsVM::class) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(true, IconType.CLOSE)

        viewModel.efgsState.observe(viewLifecycleOwner){ checked ->
            if (binding.efgsCheckbox.tag != null) {
                if (!checked) {
                    showEfgsDisableConfirmationDialog()
                }
            } else {
                // using tag as init flag to prevent dialog onCreate
                binding.efgsCheckbox.tag = true
            }
        }
    }

    private fun showEfgsDisableConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.efgs_disable_confirmation, AppConfig.efgsDays),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
            .setPositiveButton(R.string.efgs_disable_confirmation_on) { _, _ ->
                viewModel.efgsState.value = true
            }
            .setNegativeButton(R.string.efgs_disable_confirmation_off) { _, _ ->

            }.show()
    }
}