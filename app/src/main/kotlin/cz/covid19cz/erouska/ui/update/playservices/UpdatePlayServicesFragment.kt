package cz.covid19cz.erouska.ui.update.playservices

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPlayServicesUpdateBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.update.playservices.event.UpdatePlayServicesEvent

class UpdatePlayServicesFragment :
    BaseFragment<FragmentPlayServicesUpdateBinding, UpdatePlayServicesVM>(
        R.layout.fragment_play_services_update,
        UpdatePlayServicesVM::class
    ) {

    private val GMS_STORE_URL = "https://play.google.com/store/apps/details?id=com.google.android.gms"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(UpdatePlayServicesEvent::class) {
            when (it.command) {
                UpdatePlayServicesEvent.Command.PLAY_STORE -> openPlayStore()
            }
        }
    }

    private fun openPlayStore() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GMS_STORE_URL)))
    }

    override fun onBackPressed(): Boolean {
        activity?.finish()
        return true
    }

}