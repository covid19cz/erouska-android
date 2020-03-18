package cz.covid19cz.app.ui.login

sealed class LoginState
object EnterPhoneNumber: LoginState()
object AutoVerificationProgress: LoginState()
object EnterCode: LoginState()
object SigningProgress: LoginState()
data class LoginError(val exception: Exception): LoginState()
data class SignedIn(val fuid: String, val phoneNumber: String, val buid: String): LoginState()
