package cz.covid19cz.erouska.ui.confirm

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import kotlinx.android.synthetic.main.fragment_confirmation.*

class SendDataFragment : ConfirmationFragment() {
    override val description by lazy {
        getString(R.string.upload_confirmation)
    }
    override val buttonTextRes = R.string.yes_send
    override fun confirmedClicked() {
        viewModel.sendData()
    }

    override fun doWhenFinished() {
        navigate(R.id.action_nav_send_data_to_nav_send_data_success)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val text = String.format(
            getString(R.string.upload_confirmation),
            AppConfig.termsAndConditionsLink
        )
        confirm_desc.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        confirm_desc.movementMethod = LinkMovementMethod.getInstance()
    }
}
