package cz.covid19cz.erouska.ui.symptomdate.event

import arch.event.LiveEvent
import java.util.*

class DatePickerEvent(val preselect : Date?) : LiveEvent()