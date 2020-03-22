package cz.covid19cz.app.ui.login

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import androidx.navigation.NavOptions.Builder
import com.google.firebase.auth.PhoneAuthProvider
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentLoginBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.login.event.ErrorEvent
import cz.covid19cz.app.utils.Text
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

        subscribe(ErrorEvent::class) {
            when (it.command) {
                ErrorEvent.Command.ERROR_PHONE_NUMBER_INVALID_FORMAT -> {
                    login_verif_phone.isErrorEnabled = true
                    login_verif_phone.error = getString(R.string.login_phone_input_error)
                }
            }
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
        login_verif_activate_btn.setOnClickListener {
            hideKeyboard(login_verif_activate_btn)

            if (login_verif_phone_input.text?.trim()?.isNotEmpty() == true) {
                login_verif_phone.isErrorEnabled = false
                verifyPhoneNumber()
            } else {
                login_verif_phone.isErrorEnabled = true
                login_verif_phone.error = getString(R.string.login_phone_input_error)
            }
        }
        login_verif_code_send_btn.setOnClickListener {
            hideKeyboard(login_verif_code_send_btn)

            if (login_verif_code_input.text?.trim()?.isNotEmpty() == true) {
                login_verif_code.isErrorEnabled = false
                viewModel.codeEntered(login_verif_code_input.text.toString())
            } else {
                login_verif_code.isErrorEnabled = true
                login_verif_code.error = getString(R.string.login_code_input_error)
            }
        }
        login_verif_phone_input.addTextChangedListener(afterTextChanged = {
            login_verif_phone.isErrorEnabled = false
        })
        error_button_back.setOnClickListener {
            viewModel.state.postValue(EnterPhoneNumber)
        }
    }

    private fun updateState(state: LoginState) {
        when (state) {
            EnterPhoneNumber -> show(
                login_verif_image,
                login_title,
                login_desc,
                login_verif_phone,
                login_verif_phone_input,
                login_statement,
                login_verif_activate_btn
            )
            AutoVerificationProgress -> show(login_progress)
            EnterCode -> show(
                login_verif_image,
                login_verif_code_input,
                login_verif_code,
                login_verif_code_send_btn
            )
            SigningProgress -> show(login_progress)
            is LoginError -> showError(state.text)
            is SignedIn -> showSignedIn(state)
        }
    }

    private fun showSignedIn(user: SignedIn) {
        login_info.text = "Přihlášeno.\n\nFUID: ${user.fuid}\n" +
                "BUID: ${user.buid}\nTel. č.: ${user.phoneNumber}"
        show(login_info)
        waitAndOpenSandbox(2000)
    }

    private fun waitAndOpenSandbox(millisToWait: Long) {
        val timer = object : CountDownTimer(millisToWait, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //stub
            }

            override fun onFinish() {
                if (isAdded) {
                    navigate(
                        R.id.action_nav_login_to_nav_sandbox, null,
                        Builder()
                            .setPopUpTo(
                                R.id.nav_graph,
                                true
                            ).build()
                    )
                }
            }
        }
        timer.start()
    }

    private fun showError(text: Text?) {
        login_info.text = text?.toCharSequence(requireContext())
        show(login_info, error_button_back)
    }

    private fun verifyPhoneNumber() {
        val phoneNumber = login_verif_phone_input.text.toString()
        viewModel.state.postValue(AutoVerificationProgress)
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
            viewModel.state.postValue(EnterPhoneNumber)
            true
        } else false
    }
}
