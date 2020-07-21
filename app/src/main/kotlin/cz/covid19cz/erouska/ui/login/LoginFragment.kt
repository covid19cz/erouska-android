package cz.covid19cz.erouska.ui.login

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.observe
import androidx.navigation.NavOptions.Builder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentLoginBinding
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.hideKeyboard
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.BatteryOptimization
import cz.covid19cz.erouska.utils.CustomTabHelper
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.android.ext.android.inject


class LoginFragment :
    BaseFragment<FragmentLoginBinding, LoginVM>(R.layout.fragment_login, LoginVM::class) {

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

        privacy_statement.text = HtmlCompat.fromHtml(
            getString(R.string.privacy_statement, AppConfig.termsAndConditionsLink),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        privacy_statement.movementMethod = LinkMovementMethod.getInstance()
        privacy_statement.setOnClickListener { privacy_checkbox.toggle() }
    }

    private fun setupListeners() {
        privacy_statement.setOnClickListener {
            showWeb(AppConfig.termsAndConditionsLink, customTabHelper)
        }
        privacy_verif_activate_btn.setOnClickListener {
            privacyChecked()
        }
        try_again_btn.setOnClickListener {
            viewModel.activate()
        }
    }

    private fun privacyChecked() {
        if (privacy_checkbox.isChecked) {
            viewModel.activate()
        } else {
            MaterialAlertDialogBuilder(context)
                .setView(R.layout.dialog_privacy_needed)
                .setPositiveButton(R.string.confirmation_button_close)
                { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun updateState(state: LoginState) {
        when (state) {
            is ActivationStart -> onActivationStart()
            is ActivationFinished -> onActivationSuccess()
            is ActivationFailed -> onActivationFailed()
            is ActivationInit -> onActivationInit()
        }
    }

    private fun showSignedIn() {
        if (navController().currentDestination?.id == R.id.nav_login) {
            if (BatteryOptimization.isTutorialNeeded()) {
                navigate(
                    R.id.action_nav_login_to_batteryOptimizationFragment, null,
                    Builder()
                        .setPopUpTo(
                            R.id.nav_graph,
                            true
                        ).build()
                )
            } else {
                navigate(
                    R.id.action_nav_login_to_nav_dashboard, null,
                    Builder()
                        .setPopUpTo(
                            R.id.nav_graph,
                            true
                        ).build()
                )
            }
        }
    }

    private fun onActivationStart() {
        login_progress.show()

        img_privacy.hide()
        privacy_header.hide()
        privacy_checkbox.hide()
        privacy_body_1.hide()
        privacy_body_2.hide()
        privacy_verif_activate_btn.hide()
        privacy_statement.hide()
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
        privacy_checkbox.show()
        privacy_body_1.show()
        privacy_body_2.show()
        privacy_verif_activate_btn.show()
        privacy_statement.show()
        privacy_checkbox.toggle()

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
        privacy_checkbox.hide()
        privacy_body_1.hide()
        privacy_body_2.hide()
        privacy_verif_activate_btn.hide()
        privacy_statement.hide()
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
