package cz.covid19cz.erouska.ui.login

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import androidx.navigation.NavOptions.Builder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.PhoneAuthProvider
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentLoginBinding
import cz.covid19cz.erouska.ext.*
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.utils.BatteryOptimization
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.formatPhoneNumber
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class LoginFragment :
        BaseFragment<FragmentLoginBinding, LoginVM>(R.layout.fragment_login, LoginVM::class) {

    private val customTabHelper by inject<CustomTabHelper>()

//    private lateinit var views: List<View>

    override fun onStart() {
        super.onStart()
        viewModel.state.observe(this) {
            updateState(it)
        }
//        viewModel.verifyLaterShown.observe(this) {
//            if (it) {
//                login_verify_later_section.show()
//            } else {
//                login_verify_later_section.hide()
//            }
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribe(StartVerificationEvent::class) {
            verifyPhoneNumber()
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

//        views = listOf(
//            login_progress,
//            login_verif_activate_btn,
//            login_verif_code_send_btn,
//            error_message,
//            error_image,
//            login_verif_phone_input,
//            login_verif_code_input,
//            login_desc,
//            login_verif_phone,
//            login_verif_code,
//            login_statement,
//            error_button_back,
//            phone_number_code,
//            code_timeout,
//            login_checkbox,
//            login_verify_later_section,
//            error_verify_later
//        )

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
//        login_verify_later_button.setOnClickListener {
//            login_verif_code_send_btn.hideKeyboard()
//            viewModel.verifyLater()
//        }
//        error_verify_later.setOnClickListener {
//            login_verif_code_send_btn.hideKeyboard()
//            viewModel.verifyLater()
//        }
//        login_verif_phone_input.addTextChangedListener(afterTextChanged = {
//            login_verif_phone.isErrorEnabled = false
//        })
//        login_verif_phone_input.setOnDoneListener {
//            phoneNumberSent()
//        }
//        login_verif_activate_btn.setOnClickListener {
//            phoneNumberSent()
//        }
//        login_verif_code_input.addTextChangedListener(afterTextChanged = {
//            login_verif_code.isErrorEnabled = false
//        })
//        login_verif_code_input.setOnDoneListener {
//            login_verif_code_send_btn.hideKeyboard()
//            viewModel.codeEntered(login_verif_code_input.text.toString())
//        }
//        login_verif_code_send_btn.setOnClickListener {
//            login_verif_code_send_btn.hideKeyboard()
//            viewModel.codeEntered(login_verif_code_input.text.toString())
//        }
//        error_button_back.setOnClickListener {
//            goBack()
//        }
    }

    private fun privacyChecked() {
        if (privacy_checkbox.isChecked) {
            // TODO Call activation on BE
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

    private fun phoneNumberSent() {
//        if (login_checkbox.isChecked) {
//            login_verif_activate_btn.hideKeyboard()
//            viewModel.phoneNumberEntered(login_verif_phone_input.text.toString())
//        } else {
//            MaterialAlertDialogBuilder(context)
//                .setMessage(R.string.consent_warning)
//                .setPositiveButton(R.string.confirmation_button_close)
//                { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .show()
//        }
    }

    private fun updateState(state: LoginState) {
        when (state) {
            is EnterPhoneNumber -> {
//                show(
//                    login_desc,
//                    login_verif_phone,
//                    login_verif_phone_input,
//                    login_statement,
//                    login_verif_activate_btn,
//                    login_checkbox
//                )
//                login_verif_phone.isErrorEnabled = state.invalidPhoneNumber
//                if (state.invalidPhoneNumber) {
//                    login_verif_phone.error = getString(R.string.login_phone_input_error)
//                }
            }
            is EnterCode -> {
//                show(
//                    phone_number_code,
//                    login_verif_code_input,
//                    login_verif_code,
//                    login_verif_code_send_btn,
//                    code_timeout
//                )
//                phone_number_code.text = getString(
//                    R.string.login_phone_number_sms_sent,
//                    state.phoneNumber.formatPhoneNumber()
//                )
//                login_verif_code.isErrorEnabled = state.invalidCode
//                if (state.invalidCode) {
//                    login_verif_code.error = getString(R.string.login_code_input_error)
//                }
//                login_verif_code_input.focusAndShowKeyboard()
            }
            is CodeReadAutomatically -> {
//                show(
//                    phone_number_code,
//                    login_verif_code_input,
//                    login_verif_code,
//                    login_verif_code_send_btn,
//                    code_timeout
//                )
//                phone_number_code.setText(R.string.login_code_read_automatically)
//                login_verif_code.isErrorEnabled = false
//                login_verif_code_input.setText(state.code)
            }
            SigningProgress -> {
//                show(login_progress)
            }
            is ActivationStart -> {
                Toast.makeText(context, "Activation start", Toast.LENGTH_SHORT).show()
            }
            is ActivationFinished -> {
                Toast.makeText(context, "Activation end", Toast.LENGTH_SHORT).show()
            }
            is LoginError -> showError(state)
            is SignedIn -> showSignedIn()
        }

        if (state is EnterCode || state is CodeReadAutomatically) {
            activity?.setTitle(R.string.login_verification_code)
        } else {
            activity?.setTitle(R.string.login_toolbar_title)
        }
    }

    private fun showSignedIn() {
        if (navController().currentDestination?.id == R.id.nav_login) {
            if (BatteryOptimization.isTutorialNeeded()) {
                navigate(R.id.action_nav_login_to_batteryOptimizationFragment, null,
                        Builder()
                                .setPopUpTo(
                                        R.id.nav_graph,
                                        true
                                ).build())
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

    private fun showError(error: LoginError) {
//        error_message.text = error.text?.toCharSequence(requireContext())
//        if (error.allowVerifyLater) {
//            show(error_message, error_button_back, error_image, error_verify_later)
//        } else {
//            show(error_message, error_button_back, error_image)
//        }
    }

    private fun verifyPhoneNumber() {
//        val phoneNumber = login_verif_phone_input.text.toString()
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//            phoneNumber, // Phone number to verify
//            AppConfig.smsTimeoutSeconds, // Timeout duration
//            TimeUnit.SECONDS, // Unit of timeout
//            requireActivity(), // Activity (for callback binding)
//            viewModel.verificationCallbacks
//        )
    }

    private fun show(vararg views: View) {
//        views.forEach {
//            it.visibility = View.VISIBLE
//        }
//        this.views.subtract(views.toList()).forEach {
//            it.visibility = View.GONE
//        }
    }

    private fun goBack() {
        view?.hideKeyboard()
        viewModel.backPressed()
    }

    override fun onBackPressed(): Boolean {
        return if (viewModel.state.value !is EnterPhoneNumber) {
            goBack()
            true
        } else false
    }
}
