package cz.covid19cz.erouska.ui.confirm

import android.os.Bundle
import android.view.View
import com.google.firebase.storage.StorageException
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpBinding
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ext.withInternet
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.confirm.event.ErrorEvent
import cz.covid19cz.erouska.ui.confirm.event.FinishedEvent
import cz.covid19cz.erouska.ui.confirm.event.LogoutEvent
import cz.covid19cz.erouska.utils.logoutWhenNotSignedIn
import kotlinx.android.synthetic.main.fragment_confirmation.*

abstract class ConfirmationFragment : BaseFragment<FragmentHelpBinding, ConfirmationVM>(R.layout.fragment_confirmation, ConfirmationVM::class) {

    abstract val description: String
    abstract val buttonTextRes: Int
    abstract fun confirmedClicked()
    abstract fun doWhenFinished()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(FinishedEvent::class) {
            doWhenFinished()
        }
        subscribe(ErrorEvent::class) {
            confirm_desc.show()
            confirm_button.hide()
            confirm_progress.hide()
            confirm_desc.text = when ((it.exception as? StorageException)?.errorCode) {
                StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> getString(R.string.upload_error)
                else -> it.exception.message
            }
        }
        subscribe(LogoutEvent::class) {
            logoutWhenNotSignedIn()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)
        confirm_desc.text = description
        confirm_button.setText(buttonTextRes)
        confirm_button.setOnClickListener {
            requireContext().withInternet {
                confirm_desc.hide()
                confirm_button.hide()
                confirm_progress.show()
                confirmedClicked()
            }
        }
        confirm_desc.show()
        confirm_button.show()
        confirm_progress.hide()
    }
}