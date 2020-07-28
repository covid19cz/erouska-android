package cz.covid19cz.erouska.ui.sandbox

import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.base.BaseVM

class SandboxVM(
    val exposureNotificationRepo: ExposureNotificationsRepository,
    prefs : SharedPrefsRepository
) : BaseVM() {

}