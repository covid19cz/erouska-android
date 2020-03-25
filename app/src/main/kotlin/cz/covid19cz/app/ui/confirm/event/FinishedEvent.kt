package cz.covid19cz.app.ui.confirm.event

import arch.event.LiveEvent

class FinishedEvent() : LiveEvent()
data class ErrorEvent(val exception: Exception): LiveEvent()