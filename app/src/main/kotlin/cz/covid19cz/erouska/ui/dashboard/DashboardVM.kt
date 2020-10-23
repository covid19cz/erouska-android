package cz.covid19cz.erouska.ui.dashboard

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.daysSinceEpochToDateString
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

    val bluetoothState = SafeMutableLiveData(true)
    val locationState = SafeMutableLiveData(true)

    val lastUpdateDate = MutableLiveData<String>(null)
    val lastUpdateTime = MutableLiveData<String>(null)
    val lastExposureDate = MutableLiveData<String>()
    val exposuresCount = MutableLiveData(0)

    val items = ObservableArrayList<DashboardCard>()
    val allCards = mutableMapOf<Type, DashboardCard>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        prefs.lastKeyImportLive.observeForever {
            if (it != 0L) {
                lastUpdateDate.value =
                    SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(it))
                lastUpdateTime.value =
                    SimpleDateFormat("H:mm", Locale.getDefault()).format(Date(it))
            }
            L.i("last update date ${lastUpdateDate.value} at ${lastUpdateTime.value}")
            checkForObsoleteData()
        }
        exposureNotificationsEnabled.observeForever { enabled ->
            if (enabled) {
                checkForRiskyExposure()
            }
        }

        initializeOnItemChangedListener()
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {

        initializeCards()

        if (auth.currentUser == null) {
            publish(DashboardCommandEvent(DashboardCommandEvent.Command.NOT_ACTIVATED))
            return
        }

        bluetoothState.value = deviceUtils.isBtEnabled()
        locationState.value = deviceUtils.isLocationEnabled()

        checkRiskyExposures()

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

    private fun initializeCards() {
        addCard(allCards[Type.POSITIVE_TEST])
        addCard(allCards[Type.RISKY_ENCOUNTER])
        // TODO uncomment when EFGS ready
//        addCard(allCards[Type.TRAVEL])
    }

    /**
     * Takes care of correct ordering of cards in the recycler view.
     */
    private fun initializeOnItemChangedListener() {
        items.addOnListChangedCallback(object :
            ObservableList.OnListChangedCallback<ObservableArrayList<DashboardCard>>() {
            override fun onChanged(sender: ObservableArrayList<DashboardCard>?) {
            }

            override fun onItemRangeRemoved(
                sender: ObservableArrayList<DashboardCard>?,
                positionStart: Int,
                itemCount: Int
            ) {
                L.w("item inserted")
                Collections.sort(items)
                publish(DashboardCommandEvent(DashboardCommandEvent.Command.REDRAW))
            }

            override fun onItemRangeMoved(
                sender: ObservableArrayList<DashboardCard>?,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {
            }

            override fun onItemRangeInserted(
                sender: ObservableArrayList<DashboardCard>?,
                positionStart: Int,
                itemCount: Int
            ) {
                L.w("item inserted")
                Collections.sort(items)
                publish(DashboardCommandEvent(DashboardCommandEvent.Command.REDRAW))
            }

            override fun onItemRangeChanged(
                sender: ObservableArrayList<DashboardCard>?,
                positionStart: Int,
                itemCount: Int
            ) {
            }

        })
    }

    fun addCard(card: DashboardCard?) {
        if (card != null && !items.contains(card)) {
            items.add(card)
        }
    }

    fun removeCard(card: DashboardCard?) {
        items.remove(card)
    }

    /**
     * Stop the EN API.
     */
    fun stop() {
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.stop()
            }.onSuccess {
                L.d("Exposure Notifications stopped")
                onExposureNotificationsStateChanged(false)
                publish(DashboardCommandEvent(DashboardCommandEvent.Command.TURN_OFF))
            }.onFailure {
                L.e(it)
                onExposureNotificationsStateChanged(false)
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
                L.d("Exposure Notifications started")
                onExposureNotificationsStateChanged(true)
            }.onFailure {
                L.e(it)
                onExposureNotificationsStateChanged(false)
                publish(GmsApiErrorEvent(it)) // handle API error
            }
        }
    }

    /**
     * EN API state has changed.
     */
    private fun onExposureNotificationsStateChanged(enabled: Boolean) {
        exposureNotificationsEnabled.value = enabled
        prefs.setExposureNotificationsEnabled(enabled)
    }

    private fun checkForRiskyExposure() {
        viewModelScope.launch {
            runCatching {
                exposureNotificationsRepository.importLegacyExposures()
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
        L.d("Checking risky exposures")
        viewModelScope.launch {
            kotlin.runCatching {
                exposureNotificationsRepository.getAllRiskyExposures()
            }.onSuccess { riskyExposureList ->
                if (!riskyExposureList.isNullOrEmpty()) {
                    val lastExposureDate =
                        riskyExposureList.last().daysSinceEpoch.daysSinceEpochToDateString()
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
    }

    private fun onNoRiskyExposuresFound() {
        this.exposuresCount.value = 0
    }

    private fun showExposure() {
        addCard(allCards[Type.RECENT_EXPOSURE])
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

    fun onButtonClick(type: Type) {
        when (type) {
            Type.RECENT_EXPOSURE -> navigate(R.id.action_nav_dashboard_to_nav_exposures)
            Type.BLUETOOTH -> publish(DashboardCommandEvent(DashboardCommandEvent.Command.ENABLE_BT))
            Type.LOCATION_SERVICES -> publish(DashboardCommandEvent(DashboardCommandEvent.Command.ENABLE_LOCATION_SERVICES))
            Type.POSITIVE_TEST -> navigate(R.id.action_nav_dashboard_to_nav_send_data)
            Type.ACTIVE_APP -> stop()
            Type.INACTIVE_APP -> start()
        }
    }

    fun onContentClick(type: Type) {
        when (type) {
            Type.RECENT_EXPOSURE -> navigate(R.id.action_nav_dashboard_to_nav_exposures)
            Type.BLUETOOTH -> publish(DashboardCommandEvent(DashboardCommandEvent.Command.ENABLE_BT))
            Type.LOCATION_SERVICES -> publish(DashboardCommandEvent(DashboardCommandEvent.Command.ENABLE_LOCATION_SERVICES))
            Type.POSITIVE_TEST -> navigate(R.id.action_nav_dashboard_to_nav_send_data)
            Type.ACTIVE_APP -> stop()
            Type.INACTIVE_APP -> start()
            Type.RISKY_ENCOUNTER -> navigate(R.id.action_nav_dashboard_to_nav_exposures)
            Type.POSITIVE_TEST -> navigate(R.id.action_nav_dashboard_to_nav_send_data)
        }
    }

    fun onIconClick(type: Type) {
        when (type) {
            Type.RECENT_EXPOSURE -> removeCard(allCards[type])
        }
    }

}
