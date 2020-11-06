package cz.covid19cz.erouska.ui.efgs

import android.os.Bundle
import android.widget.Toast
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentEfgsBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.efgs.event.EfgsCommandEvent
import dagger.hilt.android.AndroidEntryPoint


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

        enableUpInToolbar(true, IconType.UP)
    }

    private fun turnOn() {
        // TODO
        Toast.makeText(requireContext(), "Turn EFGS On", Toast.LENGTH_SHORT).show()
    }

    private fun turnOff() {
        // TODO
        Toast.makeText(requireContext(), "Turn EFGS Off", Toast.LENGTH_SHORT).show()
    }
}