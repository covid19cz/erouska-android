package cz.covid19cz.app.ui.confirm.event

import arch.event.LiveEvent

object FinishedEvent : LiveEvent()
data class ErrorEvent(val exception: Throwable): LiveEvent()
object LogoutEvent : LiveEvent()
