package cz.covid19cz.erouska.ui.efgsagreement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentSymptomDateBinding
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsErrorHandling
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EfgsAgreementFragment : BaseFragment<FragmentSymptomDateBinding, EfgsAgreementVM>(R.layout.fragment_efgs_agreement, EfgsAgreementVM::class) {

    companion object {
        private const val SCREEN_NAME = "EFGS Agreement"
    }

    @Inject
    internal lateinit var exposureNotificationsErrorHandling: ExposureNotificationsErrorHandling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(GmsApiErrorEvent::class) {
            exposureNotificationsErrorHandling.handle(it, this, SCREEN_NAME)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ExposureNotificationsErrorHandling.REQUEST_GMS_ERROR_RESOLUTION) {
            when (resultCode) {
                Activity.RESULT_OK -> viewModel.publishKeys()
                Activity.RESULT_CANCELED -> {
                    //TODO: Integrate with Tomas's error screen
                }
            }
        }
    }

}