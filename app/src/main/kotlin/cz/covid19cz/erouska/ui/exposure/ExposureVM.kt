package cz.covid19cz.erouska.ui.exposure

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.daysSinceEpochToDateString
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.exposure.event.ExposuresCommandEvent
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExposureVM @Inject constructor(private val exposureNotificationsRepo: ExposureNotificationsRepository, private val prefs : SharedPrefsRepository) :
    BaseVM() {

    val lastExposureDate = MutableLiveData<String>()

    fun checkExposures(demo: Boolean) {
        if (!demo) {
            viewModelScope.launch {
                kotlin.runCatching {
                    exposureNotificationsRepo.getLastRiskyExposure()
                }.onSuccess {
                    publish(
                        ExposuresCommandEvent(
                            if (it != null) {
                                lastExposureDate.value = it.daysSinceEpoch.daysSinceEpochToDateString()
                                ExposuresCommandEvent.Command.RECENT_EXPOSURE
                            } else {
                                ExposuresCommandEvent.Command.NO_RECENT_EXPOSURES
                            }
                        )
                    )
                }.onFailure {
                    L.e(it)
                }
            }
        } else {
            lastExposureDate.value =
                ((System.currentTimeMillis() / 1000 / 60 / 60 / 24) - 3).toInt()
                    .daysSinceEpochToDateString()
            publish(
                ExposuresCommandEvent(ExposuresCommandEvent.Command.RECENT_EXPOSURE)
            )
        }
    }

    fun debugRecentExp() {
        publish(ExposuresCommandEvent(ExposuresCommandEvent.Command.RECENT_EXPOSURE))
    }

    fun debugExp() {
        publish(ExposuresCommandEvent(ExposuresCommandEvent.Command.NO_RECENT_EXPOSURES))
    }

}