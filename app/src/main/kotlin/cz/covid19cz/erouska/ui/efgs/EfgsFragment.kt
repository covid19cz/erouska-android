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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(EfgsCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                EfgsCommandEvent.Command.TURN_ON -> turnOn()
                EfgsCommandEvent.Command.TURN_OFF -> turnOff()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableUpInToolbar(true, IconType.CLOSE)

        efgs_checkbox.setOnCheckedChangeListener { switch, isChecked ->
            if (isChecked) {
                viewModel.turnOnEfgs()
            } else {
                showEfgsDisableConfirmationDialog(switch)
            }
        }
    }

    private fun showEfgsDisableConfirmationDialog(switch: CompoundButton) {
        AlertDialog.Builder(requireContext())
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.efgs_disable_confirmation, AppConfig.efgsDays),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
            .setPositiveButton(R.string.efgs_disable_confirmation_on) { _, _ ->
                switch.isChecked = true
            }
            .setNegativeButton(R.string.efgs_disable_confirmation_off) { _, _ ->
                switch.isChecked = false
                viewModel.turnOffEfgs()
            }.show()
    }

    private fun turnOn() {
        // no-op
    }

    private fun turnOff() {
        // no-op
    }
}