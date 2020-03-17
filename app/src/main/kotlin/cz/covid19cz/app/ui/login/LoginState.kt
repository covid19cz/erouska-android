package cz.covid19cz.app.ui.login

import com.google.firebase.auth.FirebaseUser

sealed class LoginState
object EnterPhoneNumber: LoginState()
object AutoVerificationProgress: LoginState()
object EnterCode: LoginState()
object SigningProgress: LoginState()
data class LoginError(val exception: Exception): LoginState()
data class SignedIn(val user: FirebaseUser?): LoginState()
