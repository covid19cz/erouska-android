package cz.covid19cz.erouska.ui.dashboardcards

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ext.timestampToDate
import cz.covid19cz.erouska.ext.timestampToTime

class DashboardCardsVM @ViewModelInject constructor(
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val lastUpdateDate = MutableLiveData<String>()
    val lastUpdateTime = MutableLiveData<String>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        prefs.lastKeyImportLive.observeForever {
            if (it != 0L) {
                lastUpdateDate.value = it.timestampToDate()
                lastUpdateTime.value = it.timestampToTime()
            }
        }
    }

    fun sendData() {
        navigate(R.id.action_nav_dashboard_cards_to_nav_verification)
    }
}