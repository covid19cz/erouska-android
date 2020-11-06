package cz.covid19cz.erouska.ui.update.playservices

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentPlayServicesUpdateBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.update.playservices.event.UpdatePlayServicesEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdatePlayServicesFragment :
    BaseFragment<FragmentPlayServicesUpdateBinding, UpdatePlayServicesVM>(
        R.layout.fragment_play_services_update,
        UpdatePlayServicesVM::class
    ) {

    companion object {
        const val GMS_STORE_URL = "https://play.google.com/store/apps/details?id=com.google.android.gms"
    }

    private var isDemoMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(UpdatePlayServicesEvent::class) {
            when (it.command) {
                UpdatePlayServicesEvent.Command.PLAY_STORE -> openPlayStore()
            }
        }

        isDemoMode = arguments?.let {
            UpdatePlayServicesFragmentArgs.fromBundle(it).demo
        } ?: false
    }

    override fun onResume() {
        super.onResume()
        if (!isDemoMode && !isPlayServicesObsolete()) {
            navController().navigateUp()
        }
    }

    private fun openPlayStore() {
        if (isDemoMode) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GMS_STORE_URL)))
        } else {
            if (isPlayServicesObsolete()){
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GMS_STORE_URL)))
            } else {
                navController().navigateUp()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return if (isPlayServicesObsolete()) {
            activity?.finish()
            true
        } else {
            navController().navigateUp()
            true
        }
    }

}