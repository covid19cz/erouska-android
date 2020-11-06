package cz.covid19cz.erouska.ui.activation

import arch.event.LiveEvent

sealed class ActivationState
object ActivationStart : ActivationState()
object ActivationFinished : ActivationState()
data class ActivationFailed(val errorMessage: String?) : ActivationState()
object ActivationInit : ActivationState()
object NoInternet : ActivationState()
object NotificationsVerifiedEvent : LiveEvent()