package cz.covid19cz.erouska.ui.dashboard.event

import arch.event.LiveEvent
import com.google.android.gms.common.api.Status

class GmsApiErrorEvent(val status : Status) : LiveEvent() {
}