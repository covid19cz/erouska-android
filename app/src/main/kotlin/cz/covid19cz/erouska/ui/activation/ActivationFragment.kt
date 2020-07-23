package cz.covid19cz.erouska.ui.activation

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.observe
import androidx.navigation.NavOptions.Builder
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentActivationBinding
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.hideKeyboard
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.CustomTabHelper
import kotlinx.android.synthetic.main.fragment_activation.*
import org.koin.android.ext.android.inject


class ActivationFragment :
    BaseFragment<FragmentActivationBinding, ActivationVM>(
        R.layout.fragment_activation,
        ActivationVM::class
    ) {

    private val customTabHelper by inject<CustomTabHelper>()

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
            getString(R.string.privacy_body_text_2, AppConfig.termsAndConditionsLink),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun setupListeners() {
        privacy_body_2.setOnClickListener { showWeb(AppConfig.proclamationLink, customTabHelper) }
        activate_btn.setOnClickListener { viewModel.activate() }
        try_again_btn.setOnClickListener { viewModel.activate() }
    }

    private fun updateState(state: ActivationState) {
        when (state) {
            is ActivationStart -> onActivationStart()
            is ActivationFinished -> onActivationSuccess()
            is ActivationFailed -> onActivationFailed()
            is ActivationInit -> onActivationInit()
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

        img_privacy.hide()
        privacy_header.hide()
        privacy_body_1.hide()
        privacy_body_2.hide()
        activate_btn.hide()

        img_error.hide()
        error_header.hide()
        error_body.hide()
        try_again_btn.hide()
    }

    private fun onActivationSuccess() {
        showSignedIn()
    }

    private fun onActivationInit() {
        img_error.hide()
        error_header.hide()
        error_body.hide()
        try_again_btn.hide()
        activity?.setTitle(R.string.privacy_toolbar_title)

        login_progress.hide()
        img_privacy.show()
        privacy_header.show()
        privacy_body_1.show()
        privacy_body_2.show()
        activate_btn.show()

    }

    private fun onActivationFailed() {
        img_error.show()
        error_header.show()
        error_body.show()
        try_again_btn.show()
        activity?.setTitle(R.string.activation_error_title)

        login_progress.hide()
        img_privacy.hide()
        privacy_header.hide()
        privacy_body_1.hide()
        privacy_body_2.hide()
        activate_btn.hide()
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
}
