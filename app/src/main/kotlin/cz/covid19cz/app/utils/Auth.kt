package cz.covid19cz.app.utils

import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.app.R
import cz.covid19cz.app.db.SharedPrefsRepository
import cz.covid19cz.app.service.CovidService
import cz.covid19cz.app.ui.base.BaseFragment
import org.koin.core.KoinComponent
import org.koin.core.inject

object Auth: KoinComponent {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val prefs: SharedPrefsRepository by inject()

    fun isSignedIn(): Boolean {
        return auth.currentUser != null && prefs.getDeviceBuid() != null
    }

    fun getFuid(): String {
        return checkNotNull(auth.currentUser?.uid)
    }

    fun getPhoneNumber(): String {
        return checkNotNull(auth.currentUser?.phoneNumber)
    }

    fun signOut() {
        auth.signOut()
    }
}

fun BaseFragment<*,*>.logoutWhenNotSignedIn() {
    with(requireContext()){
        startService(CovidService.stopService(this))
    }
    Auth.signOut()
    findNavController().popBackStack(R.id.nav_graph, false)
}
