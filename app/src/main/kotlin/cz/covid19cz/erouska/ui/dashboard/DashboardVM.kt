package cz.covid19cz.erouska.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.utils.DeviceUtils
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardVM @ViewModelInject constructor(
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val exposureNotificationsServerRepository: ExposureServerRepository,
    private val prefs: SharedPrefsRepository,
    private val deviceUtils: DeviceUtils
) : BaseVM() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val exposureNotificationsEnabled = SafeMutableLiveData(prefs.isExposureNotificationsEnabled())
    val permissionsState = SafeMutableLiveData(PermissionsState.BOTH_ENABLED)
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
            checkForObsoleteData()
        }
        exposureNotificationsEnabled.observeForever { enabled ->
            if (enabled) {
                checkForRiskyExposure()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (auth.currentUser == null) {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.NOT_ACTIVATED))
            return
        }

        checkBtLocationPermissions()

        exposureNotificationsServerRepository.scheduleKeyDownload()
        exposureNotificationsRepository.scheduleSelfChecker()
        checkForObsoleteData()

        viewModelScope.launch {
            kotlin.runCatching {
                return@runCatching exposureNotificationsRepository.isEnabled()
            }.onSuccess { enabled ->
                L.d("Exposure Notifications enabled $enabled")
                onExposureNotificationsStateChanged(enabled)
            }.onFailure {
                publish(GmsApiErrorEvent(it))
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
    }

    fun stop() {
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.stop()
            }.onSuccess {
                onExposureNotificationsStateChanged(false)
                L.d("Exposure Notifications stopped")
                publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_OFF))
            }.onFailure {
                L.e(it)
            }
        }
    }

    fun start() {

        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.start()
            }.onSuccess {
                onExposureNotificationsStateChanged(true)
                L.d("Exposure Notifications started")
            }.onFailure {
                onExposureNotificationsStateChanged(false)
                publish(GmsApiErrorEvent(it))
            }
        }

    }

    private fun onExposureNotificationsStateChanged(enabled: Boolean) {
        exposureNotificationsEnabled.value = enabled
        prefs.setExposureNotificationsEnabled(enabled)
    }

    private fun checkForRiskyExposure() {
        viewModelScope.launch {
            runCatching {
                exposureNotificationsRepository.getLastRiskyExposure()
            }.onSuccess {
                it?.let {
                    if (!it.accepted) {
                        showExposure()
                    }
                }
            }.onFailure {
                L.e(it)
            }
        }
    }

    private fun checkBtLocationPermissions() {
        onPermissionsStateChanged(deviceUtils.isBtEnabled(), deviceUtils.isLocationEnabled())
    }

    fun onPermissionsStateChanged(isBtEnabled: Boolean, isLocationEnabled: Boolean) {
        permissionsState.value = when {
            !isBtEnabled && !isLocationEnabled -> PermissionsState.BOTH_DISABLED
            !isLocationEnabled -> PermissionsState.LOCATION_DISABLED
            !isBtEnabled -> PermissionsState.BT_DISABLED
            else -> PermissionsState.BOTH_ENABLED
        }
    }

    private fun checkForObsoleteData() {
        if (prefs.hasOutdatedKeyData()) {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.DATA_OBSOLETE))
        } else {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.DATA_UP_TO_DATE))
        }
    }

    private fun showExposure() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.RECENT_EXPOSURE))
    }

    fun acceptExposure() {
        viewModelScope.launch {
            runCatching {
                exposureNotificationsRepository.markAsAccepted()
            }.onFailure {
                L.e(it)
            }
        }
    }

    fun unregister() {
        FirebaseAuth.getInstance().signOut()
    }

    fun sendData() {

    }

    enum class PermissionsState {
        BOTH_ENABLED, LOCATION_DISABLED, BT_DISABLED, BOTH_DISABLED
    }

}
