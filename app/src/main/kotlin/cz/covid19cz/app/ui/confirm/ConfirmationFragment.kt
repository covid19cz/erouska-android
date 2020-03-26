package cz.covid19cz.app.ui.confirm

import android.os.Bundle
import android.view.View
import com.google.firebase.storage.StorageException
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentHelpBinding
import cz.covid19cz.app.ext.withInternet
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.confirm.event.ErrorEvent
import cz.covid19cz.app.ui.confirm.event.FinishedEvent
import cz.covid19cz.app.utils.hide
import cz.covid19cz.app.utils.show
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