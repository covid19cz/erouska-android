package cz.covid19cz.erouska.ui.login

import cz.covid19cz.erouska.utils.Text

sealed class LoginState
data class EnterPhoneNumber(val invalidPhoneNumber: Boolean): LoginState()
object StartVerification: LoginState()
data class EnterCode(val invalidCode: Boolean, val phoneNumber: String): LoginState()
data class CodeReadAutomatically(val code: String): LoginState()
object SigningProgress: LoginState()
data class LoginError(val text: Text?): LoginState()
object SignedIn: LoginState()
