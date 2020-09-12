package cz.covid19cz.erouska.ui.exposure

import androidx.lifecycle.viewModelScope
import arch.event.SingleLiveEvent
import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ext.daysSinceEpochToDateString
import cz.covid19cz.erouska.ui.exposure.event.RecentExposuresEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecentExposuresVM(private val exposureNotificationsRepo: ExposureNotificationsRepository) :
    BaseArchViewModel() {

    val state = SingleLiveEvent<RecentExposuresEvent>()

    fun loadExposures(demo: Boolean = false) {
        if (!demo) {
            viewModelScope.launch {
                kotlin.runCatching {
                    exposureNotificationsRepo.getDailySummaries()
                }.onSuccess { dailySummaries ->
                    if (dailySummaries.isNotEmpty()) {
                        val exposureList = emptyList<Exposure>()
                        for (dailySummary in dailySummaries) {
                            exposureList.plus(Exposure(dailySummary.daysSinceEpoch.daysSinceEpochToDateString()))
                        }
                        state.postValue(RecentExposuresEvent.ExposuresLoadedEvent(exposureList))
                    }
                }.onFailure {
                    L.e(it)
                }
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val mockData = listOf(
                    Exposure("28. prosince 2019"),
                    Exposure("20. března 2019"),
                    Exposure("14. května 2019"),
                    Exposure("5. srpna 2019")
                )

                state.postValue(RecentExposuresEvent.ExposuresLoadedEvent(mockData))
            }
        }

    }
}
