package cz.covid19cz.erouska.ui.newsletter

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentNewsletterBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.newsletter.event.NewsletterEvent
import kotlinx.android.synthetic.main.fragment_newsletter.*

class NewsletterFragment : BaseFragment<FragmentNewsletterBinding, NewsletterVM>(
    R.layout.fragment_newsletter,
    NewsletterVM::class
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.state.observe(this) {
            when (it) {
                NewsletterEvent.NewsletterExpansion -> showExpansionNews()
                NewsletterEvent.NewsletterPhoneNumbers -> showPhoneNumberNews()
                NewsletterEvent.NewsletterActiveNotification -> showActiveNotificationNews()
                NewsletterEvent.NewsletterAccessible -> showAccessibleNews()
                NewsletterEvent.NewsletterPrivacy -> showPrivacyNews()
                NewsletterEvent.NewsletterFinish -> finish()
            }
        }

    }

    private fun showExpansionNews() {
        enableUpInToolbar(true, IconType.CLOSE)
        newsletter_img.setImageResource(R.drawable.ic_newsletter_expansion)
        newsletter_header.text = getString(R.string.newsletter_expansion_header)
        newsletter_body.text = getString(R.string.newsletter_expansion_body)
        newsletter_button.text = getString(R.string.newsletter_button_continue)
        newsletter_button.setOnClickListener { next() }
    }

    private fun showActiveNotificationNews() {
        enableUpInToolbar(true, IconType.UP)
        newsletter_img.setImageResource(R.drawable.ic_newsletter_active_notification)
        newsletter_header.text = getString(R.string.newsletter_active_notification_header)
        newsletter_body.text = getString(R.string.newsletter_active_notification_body)
        newsletter_button.text = getString(R.string.newsletter_button_continue)
        newsletter_button.setOnClickListener { next() }
    }

    private fun showPhoneNumberNews() {
        enableUpInToolbar(true, IconType.UP)
        newsletter_img.setImageResource(R.drawable.ic_newsletter_phone)
        newsletter_header.text = getString(R.string.newsletter_phone_header)
        newsletter_body.text = getString(R.string.newsletter_phone_body)
        newsletter_button.text = getString(R.string.newsletter_button_continue)
        newsletter_button.setOnClickListener { next() }
    }

    private fun showAccessibleNews() {
        enableUpInToolbar(true, IconType.UP)
        newsletter_img.setImageResource(R.drawable.ic_newsletter_accessible)
        newsletter_header.text = getString(R.string.newsletter_accessible_header)
        newsletter_body.text = getString(R.string.newsletter_accessible_body)
        newsletter_button.text = getString(R.string.newsletter_button_continue)
        newsletter_button.setOnClickListener { next() }
    }

    private fun showPrivacyNews() {
        enableUpInToolbar(true, IconType.UP)
        newsletter_img.setImageResource(R.drawable.ic_newsletter_privacy)
        newsletter_header.text = getString(R.string.newsletter_privacy_header)
        newsletter_body.text = getString(R.string.newsletter_privacy_body)
        newsletter_button.text = getString(R.string.newsletter_button_close)
        newsletter_button.setOnClickListener { finish() }
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
        return viewModel.state.value == NewsletterEvent.NewsletterExpansion
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
