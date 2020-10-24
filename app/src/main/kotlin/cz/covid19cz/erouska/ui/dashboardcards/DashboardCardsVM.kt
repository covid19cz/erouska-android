package cz.covid19cz.erouska.ui.dashboardcards

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import java.text.SimpleDateFormat
import java.util.*

class DashboardCardsVM @ViewModelInject constructor(
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    val lastUpdateDate = MutableLiveData<String>()
    val lastUpdateTime = MutableLiveData<String>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        prefs.lastKeyImportLive.observeForever {
            if (it != 0L) {
                lastUpdateDate.value =
                    SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(it))
                lastUpdateTime.value =
                    SimpleDateFormat("H:mm", Locale.getDefault()).format(Date(it))
            }
        }
    }

    fun sendData() {
        navigate(R.id.action_nav_dashboard_cards_to_nav_send_data)
    }
}