package cz.covid19cz.erouska.ui.mydata.event

import arch.event.LiveEvent

class MyDataCommandEvent(val command: Command) : LiveEvent() {

    enum class Command{
        MEASURES,
    }

}