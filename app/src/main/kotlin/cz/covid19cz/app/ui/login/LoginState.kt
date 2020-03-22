package cz.covid19cz.app.ui.login

import cz.covid19cz.app.utils.Text

sealed class LoginState
object EnterPhoneNumber: LoginState()
object AutoVerificationProgress: LoginState()
object EnterCode: LoginState()
object SigningProgress: LoginState()
data class LoginError(val text: Text?): LoginState()
data class SignedIn(val fuid: String, val phoneNumber: String, val buid: String): LoginState()
