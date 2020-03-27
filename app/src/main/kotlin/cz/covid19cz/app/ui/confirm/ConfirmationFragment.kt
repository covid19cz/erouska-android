package cz.covid19cz.app.ui.confirm

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.storage.StorageException
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentHelpBinding
import cz.covid19cz.app.ext.withInternet
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.confirm.event.ErrorEvent
import cz.covid19cz.app.ui.confirm.event.FinishedEvent
import cz.covid19cz.app.ui.confirm.event.LogoutEvent
import cz.covid19cz.app.utils.hide
import cz.covid19cz.app.utils.logoutWhenNotSignedIn
import cz.covid19cz.app.utils.show
import kotlinx.android.synthetic.main.fragment_confirmation.*

abstract class ConfirmationFragment : BaseFragment<FragmentHelpBinding, ConfirmationVM>(R.layout.fragment_confirmation, ConfirmationVM::class) {

    abstract val confirmDescription: String
    abstract val successShortText: String
    open val successDescription: String = ""
    abstract val confirmButtonTextRes: Int

    abstract fun doOnConfirm()
    abstract fun doOnClose()
    open fun doOnSuccess() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(FinishedEvent::class) {
            showSuccessScreen()
            doOnSuccess()
        }
        subscribe(ErrorEvent::class) {
            confirm_button.hide()
            confirm_progress.hide()
            description.text = when ((it.exception as? StorageException)?.errorCode) {
                StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> getString(R.string.upload_error)
                else -> it.exception.message
            }
            description.show()
        }
        subscribe(LogoutEvent::class) {
            logoutWhenNotSignedIn()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)
        description.text = confirmDescription
        confirm_button.setText(confirmButtonTextRes)
        confirm_button.setOnClickListener {
            requireContext().withInternet {
                description.hide()
                confirm_button.hide()
                confirm_progress.show()
                doOnConfirm()
            }
        }
        close_button.setOnClickListener {
            doOnClose()
        }
    }

    private fun showSuccessScreen() {
        confirm_progress.hide()
        success_image.text = successShortText
        success_image.show()

        // move success image to center if there is no text
        if (successDescription.isBlank()) {
            val params = success_image.layoutParams as ConstraintLayout.LayoutParams
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        }

        description.text = successDescription
        description.show()
        close_button.show()
    }
}
