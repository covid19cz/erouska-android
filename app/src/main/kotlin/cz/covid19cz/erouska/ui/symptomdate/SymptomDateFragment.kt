package cz.covid19cz.erouska.ui.symptomdate

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSymptomDateBinding
import cz.covid19cz.erouska.ext.hideKeyboard
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.symptomdate.event.DatePickerEvent
import cz.covid19cz.erouska.ui.symptomdate.event.SymptomDateCommandEvent
import cz.covid19cz.erouska.ui.symptomdate.event.SymptomDateCommandEvent.Command.NAV_EFGS_AGREEMENT
import cz.covid19cz.erouska.ui.symptomdate.event.SymptomDateCommandEvent.Command.NAV_TRAVELLER
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SymptomDateFragment : BaseFragment<FragmentSymptomDateBinding, SymptomDateVM>(
    R.layout.fragment_symptom_date,
    SymptomDateVM::class
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.hideKeyboard()
        subscribe(DatePickerEvent::class) {
            showDatePickerDialog(it.preselect)
        }
        subscribe(SymptomDateCommandEvent::class) {
            when (it.command) {
                NAV_TRAVELLER -> navigate(R.id.action_nav_symptom_date_to_nav_traveller)
                NAV_EFGS_AGREEMENT -> navigate(R.id.action_nav_symptom_date_to_efgsAgreementFragment)
            }
        }

    }

    private fun showDatePickerDialog(preselect: Date?) {
        val preselectCalendar = Calendar.getInstance()
        if (preselect != null) {
            preselectCalendar.time = preselect
        }
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view, year, month, day ->
                viewModel.symptomDate.value = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }.time
            },
            preselectCalendar.get(Calendar.YEAR),
            preselectCalendar.get(Calendar.MONTH),
            preselectCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -14) }.timeInMillis
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog.show()
    }

}