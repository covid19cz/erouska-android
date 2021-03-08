package cz.covid19cz.erouska.ui.activation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.NavOptions.Builder
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentActivationBinding
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsErrorHandling
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.hideKeyboard
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.SupportEmailGenerator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_activation.*
import javax.inject.Inject

@AndroidEntryPoint
class ActivationFragment :
    BaseFragment<FragmentActivationBinding, ActivationVM>(
        R.layout.fragment_activation,
        ActivationVM::class
    ) {

    companion object {
        private const val SCREEN_NAME = "Activation"
    }

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper

    @Inject
    internal lateinit var exposureNotificationsErrorHandling: ExposureNotificationsErrorHandling

    @Inject
    internal lateinit var supportEmailGenerator: SupportEmailGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(GmsApiErrorEvent::class) {
            exposureNotificationsErrorHandling.handle(it, this, SCREEN_NAME)
        }

    }

    override fun onStart() {
        super.onStart()
        viewModel.state.observe(this) {
            updateState(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.onboarding, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val handled = onBackPressed()
                return if (handled) {
                    true
                } else {
                    super.onOptionsItemSelected(item)
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

        enableUpInToolbar(true)

        privacy_body_2.text = HtmlCompat.fromHtml(
            getString(R.string.privacy_body_text_2, AppConfig.conditionsOfUseUrl),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun setupListeners() {
        privacy_body_2.setOnClickListener { showWeb(AppConfig.conditionsOfUseUrl, customTabHelper) }
        activate_btn.setOnClickListener { viewModel.activate() }
    }

    private fun updateState(state: ActivationState) {
        when (state) {
            is ActivationStart -> onActivationStart()
            is ActivationFinished -> onActivationSuccess()
            is ActivationFailed -> onActivationFailed(state.errorMessage)
            is ActivationInit -> onActivationInit()
            is NoInternet -> onNoInternet()
        }
    }

    private fun showSignedIn() {
        if (navController().currentDestination?.id == R.id.nav_activation) {
            navigate(
                R.id.action_nav_activation_to_nav_dashboard,
                null,
                Builder().setPopUpTo(R.id.nav_graph, true).build()
            )
        }
    }

    private fun onActivationStart() {
        login_progress.show()

        activate_btn.hide()
        privacy_group.hide()
        error_group.hide()
    }

    private fun onActivationSuccess() {
        showSignedIn()
    }

    private fun onNoInternet() {
        showSnackBar(R.string.no_internet)
    }

    private fun onActivationInit() {
        activity?.setTitle(R.string.privacy_toolbar_title)

        login_progress.hide()

        activate_btn.show()
        privacy_group.show()
        error_group.hide()
    }

    private fun onActivationFailed(errorMessage: String?) {
        activity?.setTitle(R.string.error_title)
        error_body.text =
            getString(R.string.send_data_failure_body, AppConfig.supportEmail, errorMessage)
        support_button.setOnClickListener {
            supportEmailGenerator.sendSupportEmail(
                requireActivity(),
                lifecycleScope,
                errorCode = errorMessage,
                isError = true,
                screenOrigin = SCREEN_NAME
            )
        }
        login_progress.hide()

        privacy_group.hide()
        activate_btn.hide()
        error_group.show()
    }

    private fun goBack() {
        view?.hideKeyboard()
        viewModel.backPressed()
    }

    override fun onBackPressed(): Boolean {
        return if (viewModel.state.value is ActivationFailed) {
            goBack()
            true
        } else false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ExposureNotificationsErrorHandling.REQUEST_GMS_ERROR_RESOLUTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.activate()
                }
            }
        }
    }
}
