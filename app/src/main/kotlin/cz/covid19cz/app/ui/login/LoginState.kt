package cz.covid19cz.app.ui.login

import cz.covid19cz.app.utils.Text

sealed class LoginState
data class EnterPhoneNumber(val invalidPhoneNumber: Boolean): LoginState()
object StartVerification: LoginState()
data class EnterCode(val invalidCode: Boolean): LoginState()
data class CodeReadAutomatically(val code: String): LoginState()
object SigningProgress: LoginState()
data class LoginError(val text: Text?): LoginState()
data class SignedIn(val fuid: String, val phoneNumber: String, val buid: String): LoginState()
