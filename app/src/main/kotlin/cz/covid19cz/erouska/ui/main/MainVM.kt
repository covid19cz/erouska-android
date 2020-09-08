package cz.covid19cz.erouska.ui.main

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import arch.livedata.SafeMutableLiveData
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM

class MainVM: BaseVM() {

    val serviceRunning = SafeMutableLiveData(false)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        if (auth.currentUser == null) {
            setNavigationGraph(R.navigation.nav_graph, R.id.nav_welcome_fragment)
        } else {
            setNavigationGraph(R.navigation.nav_graph, R.id.nav_dashboard)
        }
    }

}