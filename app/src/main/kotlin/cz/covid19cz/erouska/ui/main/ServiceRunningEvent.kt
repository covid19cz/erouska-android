package cz.covid19cz.erouska.ui.main

import arch.event.LiveEvent

class ServiceRunningEvent(val running: Boolean) : LiveEvent()