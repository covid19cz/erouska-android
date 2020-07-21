package cz.covid19cz.erouska.ui.sandbox

import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepo
import cz.covid19cz.erouska.ui.base.BaseVM

class SandboxVM(
    val exposureNotificationRepo: ExposureNotificationsRepo,
    prefs : SharedPrefsRepository
) : BaseVM() {

}