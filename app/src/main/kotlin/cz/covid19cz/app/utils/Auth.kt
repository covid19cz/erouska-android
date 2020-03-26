package cz.covid19cz.app.utils

import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.app.db.SharedPrefsRepository
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