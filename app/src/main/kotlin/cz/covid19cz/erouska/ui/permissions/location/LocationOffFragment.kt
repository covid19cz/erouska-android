package cz.covid19cz.erouska.ui.permissions.location

import android.os.Bundle
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentLocationOffBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.permissions.location.event.LocationEvent

class LocationOffFragment : BaseFragment<FragmentLocationOffBinding, LocationOffVM>(
    R.layout.fragment_location_off,
    LocationOffVM::class
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(LocationEvent::class) {
            when (it.command) {
                LocationEvent.Command.ENABLE_LOCATION -> requestLocationEnable()
            }
        }
    }


}