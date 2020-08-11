package cz.covid19cz.erouska.ui.update

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.observe
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentLegacyUpdateBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.update.event.LegacyUpdateEvent
import kotlinx.android.synthetic.main.fragment_legacy_update.*

class LegacyUpdateFragment : BaseFragment<FragmentLegacyUpdateBinding, LegacyUpdateVM>(
    R.layout.fragment_legacy_update,
    LegacyUpdateVM::class
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.observe(this) {
            when (it) {
                LegacyUpdateEvent.LegacyUpdateExpansion -> showExpansionNews()
                LegacyUpdateEvent.LegacyUpdatePhoneNumbers -> showPhoneNumberNews()
                LegacyUpdateEvent.LegacyUpdateActiveNotification -> showActiveNotificationNews()
                LegacyUpdateEvent.LegacyUpdatePrivacy -> showPrivacyNews()
                LegacyUpdateEvent.LegacyUpdateFinish -> finish()
            }
        }

    }

    private fun showExpansionNews() {
        enableUpInToolbar(true, IconType.CLOSE)
        legacy_update_img.setImageResource(R.drawable.ic_update_expansion)
        legacy_update_header.text = getString(R.string.legacy_update_expansion_header)
        legacy_update_body.text = getString(R.string.legacy_update_expansion_body)
        legacy_update_button.text = getString(R.string.legacy_update_button_continue)
        legacy_update_button.setOnClickListener { next() }
    }

    private fun showActiveNotificationNews() {
        enableUpInToolbar(true, IconType.UP)
        legacy_update_img.setImageResource(R.drawable.ic_update_active_notification)
        legacy_update_header.text = getString(R.string.legacy_update_active_notification_header)
        legacy_update_body.text = getString(R.string.legacy_update_active_notification_body)
        legacy_update_button.text = getString(R.string.legacy_update_button_continue)
        legacy_update_button.setOnClickListener { next() }
    }

    private fun showPhoneNumberNews() {
        enableUpInToolbar(true, IconType.UP)
        legacy_update_img.setImageResource(R.drawable.ic_update_phone)
        legacy_update_header.text = getString(R.string.legacy_update_phone_header)
        legacy_update_body.text = getString(R.string.legacy_update_phone_body)
        legacy_update_button.text = getString(R.string.legacy_update_button_continue)
        legacy_update_button.setOnClickListener { next() }
    }

    private fun showPrivacyNews() {
        enableUpInToolbar(true, IconType.UP)
        legacy_update_img.setImageResource(R.drawable.ic_update_privacy)
        legacy_update_header.text = getString(R.string.legacy_update_privacy_header)
        legacy_update_body.text = getString(R.string.legacy_update_privacy_body)
        legacy_update_button.text = getString(R.string.legacy_update_button_close)
        legacy_update_button.setOnClickListener { finish() }
    }

    private fun finish() {
        viewModel.finish()
        navController().navigateUp()
    }

    private fun next() {
        viewModel.next()
    }

    private fun previous() {
        viewModel.previous()
    }

    private fun isFirstScreen(): Boolean {
        return viewModel.state.value == LegacyUpdateEvent.LegacyUpdateExpansion
    }

    override fun onBackPressed(): Boolean {
        return if (!isFirstScreen()) {
            previous()
            true
        } else {
            activity?.finish()
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return onBackPressed()
    }

}
