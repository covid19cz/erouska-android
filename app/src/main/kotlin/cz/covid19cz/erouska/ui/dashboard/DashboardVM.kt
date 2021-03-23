package cz.covid19cz.erouska.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatus
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.daysSinceEpochToDateString
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.exposure.event.ExposuresCommandEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.launch

class DashboardVM
@ViewModelInject constructor(
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val exposureNotificationsServerRepository: ExposureServerRepository,
    private val prefs: SharedPrefsRepository
) : BaseVM() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val exposureNotificationsEnabled = SafeMutableLiveData(prefs.isExposureNotificationsEnabled())

    val bluetoothState = SafeMutableLiveData(true)
    val locationState = SafeMutableLiveData(true)
    val efgsState = SafeMutableLiveData(prefs.isTraveller())

    val lastUpdateTimestamp = MutableLiveData(prefs.getLastKeyImport())
    val lastExposureDate = MutableLiveData<String>()
    val exposuresCount = MutableLiveData(0)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        prefs.lastKeyImportLive.observeForever {
            updateLastKeyImport(it)
            checkForObsoleteData()
            checkAndShowOrHideHowItWorksNotification()
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
        efgsState.value = prefs.isTraveller()
        checkStatus()
        checkRiskyExposures()

        exposureNotificationsServerRepository.scheduleKeyDownload()
        exposureNotificationsRepository.scheduleSelfChecker()
        checkForObsoleteData()
        checkAndShowOrHideHowItWorksNotification()
    }

    private fun updateLastKeyImport(lastKeyImport: Long) {
        if (lastKeyImport != 0L) {
            lastUpdateTimestamp.value = lastKeyImport
        }
    }

    fun checkStatus() {
        viewModelScope.launch {
            kotlin.runCatching {
                if (!exposureNotificationsRepository.isEnabled()) {
                    return@runCatching setOf(ExposureNotificationStatus.INACTIVATED)
                }
                return@runCatching exposureNotificationsRepository.getStatus()
            }.onSuccess { status ->
                L.i("EN API Status: ${status.joinToString { it.name }}")
                onExposureNotificationsStatusChanged(status)
            }.onFailure {
                publish(GmsApiErrorEvent(it))
            }
        }
    }

    /**
     * Stop the EN API.
     */
    fun stop() {
        viewModelScope.launch {
            kotlin.runCatching {
                if (exposureNotificationsRepository.isEnabled()) {
                    exposureNotificationsRepository.stop()
                }
            }.onSuccess {
                L.i("EN API Stopped")
                checkStatus()
                publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_OFF))
            }.onFailure {
                L.e(it)
                publish(GmsApiErrorEvent(it)) // handle API error
            }
        }
    }

    /**
     * Start the EN API.
     */
    fun start() {
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.start()
            }.onSuccess {
                L.i("EN API Started")
                checkStatus()
            }.onFailure {
                L.e(it)
                publish(GmsApiErrorEvent(it)) // handle API error
            }
        }
    }

    /**
     * EN API state has changed.
     */
    private fun onExposureNotificationsStatusChanged(status: Set<ExposureNotificationStatus>) {
        bluetoothState.value = !status.contains(ExposureNotificationStatus.BLUETOOTH_DISABLED)
        locationState.value = !status.contains(ExposureNotificationStatus.LOCATION_DISABLED)
        exposureNotificationsEnabled.value = status.contains(ExposureNotificationStatus.ACTIVATED)
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

    private fun checkForObsoleteData() {
        if (prefs.hasOutdatedKeyData()) {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.DATA_OBSOLETE))
        } else {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.DATA_UP_TO_DATE))
        }
    }

    /**
     * Check the database for recent risky exposures.
     */
    private fun checkRiskyExposures() {
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.getDailySummariesFromDbByExposureDate()
            }.onSuccess { riskyExposureList ->
                if (!riskyExposureList.isNullOrEmpty()) {
                    val lastExposureDate =
                        riskyExposureList.first().daysSinceEpoch.daysSinceEpochToDateString()
                    onRiskyExposuresFound(riskyExposureList.size, lastExposureDate)
                } else {
                    onNoRiskyExposuresFound()
                }
            }.onFailure {
                L.e(it)
            }
        }
    }

    private fun onRiskyExposuresFound(count: Int, lastExposureDate: String) {
        this.lastExposureDate.value = lastExposureDate
        this.exposuresCount.value = count
        publish(ExposuresCommandEvent(ExposuresCommandEvent.Command.RECENT_EXPOSURE))
    }

    private fun onNoRiskyExposuresFound() {
        publish(ExposuresCommandEvent(ExposuresCommandEvent.Command.NO_RECENT_EXPOSURES))
    }

    private fun showExposure() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.RECENT_EXPOSURE))
    }

    fun showExposureDetail() {
        viewModelScope.launch {
            val lastRiskyExposure = exposureNotificationsRepository.getLastRiskyExposure()
            if (lastRiskyExposure != null && lastRiskyExposure.daysSinceEpoch > prefs.getLastShownExposureInfo()) {
                navigate(DashboardFragmentDirections.actionNavDashboardToNavExposureInfo())
            } else {
                navigate(DashboardFragmentDirections.actionNavDashboardToNavExposure())
            }
        }
    }

    fun showEfgs() {
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.EFGS))
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
        navigate(R.id.action_nav_dashboard_to_nav_verification)
    }

    fun shouldIntroduceEFGS(): Boolean {
        return !prefs.wasEFGSIntroduced() && !prefs.shouldSuppressUpdateScreens()
    }

    fun cancelSuppression() {
        return prefs.setSuppressUpdateScreens(false)
    }

    fun isLocationlessScanSupported() = exposureNotificationsRepository.isLocationlessScanSupported()

    fun checkAndShowOrHideHowItWorksNotification() {
        if (prefs.wasHowItWorksShown()) {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.HIDE_HOW_IT_WORKS))
        } else {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.SHOW_HOW_IT_WORKS))
        }
    }

    fun dismissHowItWorksNotification() {
        prefs.setHowItWorksShown()
        publish(DashboardCommandEvent(DashboardCommandEvent.Command.HIDE_HOW_IT_WORKS))
    }

    fun showHowItWorksPage() {
        navigate(DashboardFragmentDirections.actionNavDashboardToNavHowItWorks())
    }
}
