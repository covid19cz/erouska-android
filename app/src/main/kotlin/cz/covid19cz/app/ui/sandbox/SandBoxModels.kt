package cz.covid19cz.app.ui.sandbox

import arch.event.LiveEvent

sealed class ExportEvent : LiveEvent() {

    data class Complete(val fileName: String) : ExportEvent()
    data class Error(val message: String) : ExportEvent()
}