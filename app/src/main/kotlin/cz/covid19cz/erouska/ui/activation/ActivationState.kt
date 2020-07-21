package cz.covid19cz.erouska.ui.activation

import arch.event.LiveEvent

sealed class ActivationState
object ActivationStart : ActivationState()
object ActivationFinished : ActivationState()
object ActivationFailed : ActivationState()
object ActivationInit : ActivationState()
//data class EnterPhoneNumber(val invalidPhoneNumber: Boolean) : LoginState()
//data class EnterCode(val invalidCode: Boolean, val phoneNumber: String) : LoginState()
//data class CodeReadAutomatically(val code: String) : LoginState()
//object SigningProgress : LoginState()
//data class LoginError(val text: Text?, val allowVerifyLater: Boolean) : LoginState()
//object SignedIn : LoginState()

object StartVerificationEvent : LiveEvent()
