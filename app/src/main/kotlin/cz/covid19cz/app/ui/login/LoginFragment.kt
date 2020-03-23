package cz.covid19cz.app.ui.login

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import androidx.navigation.NavOptions.Builder
import com.google.firebase.auth.PhoneAuthProvider
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentLoginBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.utils.Text
import cz.covid19cz.app.utils.setOnDoneListener
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.concurrent.TimeUnit

class LoginFragment :
    BaseFragment<FragmentLoginBinding, LoginVM>(R.layout.fragment_login, LoginVM::class) {

    private lateinit var views: List<View>

    override fun onStart() {
        super.onStart()
        viewModel.state.observe(this) {
            updateState(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.help, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views = listOf(
            login_verif_image,
            login_progress,
            login_verif_activate_btn,
            login_verif_code_send_btn,
            login_info,
            login_verif_phone_input,
            login_verif_code_input,
            login_title,
            login_desc,
            login_verif_phone,
            login_verif_code,
            login_statement,
            error_button_back
        )

        setupListeners()

        enableUpInToolbar(true)
    }

    private fun setupListeners() {
        login_verif_phone_input.addTextChangedListener(afterTextChanged = {
            login_verif_phone.isErrorEnabled = false
        })
        login_verif_phone_input.setOnDoneListener {
            hideKeyboard(login_verif_activate_btn)
            viewModel.phoneNumberEntered(login_verif_phone_input.text.toString())
        }
        login_verif_activate_btn.setOnClickListener {
            hideKeyboard(login_verif_activate_btn)
            viewModel.phoneNumberEntered(login_verif_phone_input.text.toString())
        }
        login_verif_code_input.addTextChangedListener(afterTextChanged = {
            login_verif_code.isErrorEnabled = false
        })
        login_verif_code_input.setOnDoneListener {
            hideKeyboard(login_verif_code_send_btn)
            viewModel.codeEntered(login_verif_code_input.text.toString())
        }
        login_verif_code_send_btn.setOnClickListener {
            hideKeyboard(login_verif_code_send_btn)
            viewModel.codeEntered(login_verif_code_input.text.toString())
        }
        error_button_back.setOnClickListener {
            viewModel.backPressed()
        }
    }

    private fun updateState(state: LoginState) {
        when (state) {
            is EnterPhoneNumber -> {
                show(
                    login_verif_image,
                    login_title,
                    login_desc,
                    login_verif_phone,
                    login_verif_phone_input,
                    login_statement,
                    login_verif_activate_btn
                )
                login_verif_phone.isErrorEnabled = state.invalidPhoneNumber
                if (state.invalidPhoneNumber) {
                    login_verif_phone.error = getString(R.string.login_phone_input_error)
                }
            }
            is EnterCode -> {
                show(
                    login_verif_image,
                    login_verif_code_input,
                    login_verif_code,
                    login_verif_code_send_btn
                )
                login_verif_code.isErrorEnabled = state.invalidCode
                if (state.invalidCode) {
                    login_verif_code.error = getString(R.string.login_code_input_error)
                }
            }
            SigningProgress -> show(login_progress)
            is LoginError -> showError(state.text)
            is SignedIn -> showSignedIn()
            StartVerification -> verifyPhoneNumber()
        }
    }

    private fun showSignedIn() {
        if (navController().currentDestination?.id == R.id.nav_login) {
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

    private fun showError(text: Text?) {
        login_info.text = text?.toCharSequence(requireContext())
        show(login_info, error_button_back)
    }

    private fun verifyPhoneNumber() {
        show(login_progress)
        val phoneNumber = login_verif_phone_input.text.toString()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            AppConfig.smsTimeoutSeconds, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            requireActivity(), // Activity (for callback binding)
            viewModel.verificationCallbacks
        )
    }

    private fun show(vararg views: View) {
        views.forEach {
            it.visibility = View.VISIBLE
        }
        this.views.subtract(views.toList()).forEach {
            it.visibility = View.GONE
        }
    }

    private fun hideKeyboard(view: View) {
        val im = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(view.windowToken, 0);
    }

    override fun onBackPressed(): Boolean {
        return if (viewModel.state.value is LoginError) {
            viewModel.backPressed()
            true
        } else false
    }
}
