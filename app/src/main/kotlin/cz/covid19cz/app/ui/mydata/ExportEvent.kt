package cz.covid19cz.app.ui.mydata

import arch.event.LiveEvent

sealed class ExportEvent : LiveEvent() {

    object Complete : ExportEvent()
    data class Error(val message: String) : ExportEvent()
    data class PleaseWait(val minutes: Int): ExportEvent()
    object Confirmation: ExportEvent()
}