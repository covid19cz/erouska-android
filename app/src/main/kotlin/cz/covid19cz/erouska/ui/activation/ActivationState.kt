package cz.covid19cz.erouska.ui.activation

import arch.event.LiveEvent

sealed class ActivationState
object ActivationStart : ActivationState()
object ActivationFinished : ActivationState()
object ActivationFailed : ActivationState()
object ActivationInit : ActivationState()

object StartVerificationEvent : LiveEvent()
