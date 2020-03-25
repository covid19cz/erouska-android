package cz.covid19cz.app.ui.help

import android.os.Build
import cz.covid19cz.app.AppConfig
import cz.covid19cz.app.ui.base.BaseVM
import cz.covid19cz.app.ui.help.event.HelpCommandEvent

class HelpVM : BaseVM() {

    fun goBack() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.GO_BACK))
    }

    fun getProclamationUrl(): String {
        return AppConfig.proclamationDynamicLink
    }

    fun getTutorialUrl(): String {
        return AppConfig.tutorialDynamicLink //"http://www.erouska.cz/navody"
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String): String {
        if (s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first).toString() + s.substring(1)
        }
    }
}