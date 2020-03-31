package cz.covid19cz.erouska.ui.success

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R

class SendDataSuccessFragment : SuccessFragment() {

    override val title = R.string.upload_data_success_text
    override val description = R.string.upload_data_success_description

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.CLOSE)
    }
}