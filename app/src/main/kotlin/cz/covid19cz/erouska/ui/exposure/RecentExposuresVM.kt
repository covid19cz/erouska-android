package cz.covid19cz.erouska.ui.exposure

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import arch.event.SingleLiveEvent
import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.exposure.event.RecentExposuresEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class RecentExposuresVM @ViewModelInject constructor(private val exposureNotificationsRepo: ExposureNotificationsRepository) :
    BaseArchViewModel() {

    val state = SingleLiveEvent<RecentExposuresEvent>()

    fun loadExposures(demo: Boolean = false) {
        if (!demo) {
            viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    exposureNotificationsRepo.getDailySummaries()
                }.onSuccess { dailySummaries ->
                    if (dailySummaries.isNotEmpty()) {
                        val exposureList = dailySummaries.map {
                            Exposure(it.daysSinceEpoch)
                        }
                        state.postValue(RecentExposuresEvent.ExposuresLoadedEvent(exposureList))
                    } else {
                        state.postValue(RecentExposuresEvent.NoExposuresEvent)
                    }
                }.onFailure {
                    state.postValue(RecentExposuresEvent.NoExposuresEvent)
                    L.e(it)
                }
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val now = LocalDate.now()
                val mockData = listOf(
                    // old exposures
                    Exposure(LocalDate.of(2019, 12, 28).toEpochDay().toInt()),
                    Exposure(LocalDate.of(2019, 3, 20).toEpochDay().toInt()),
                    Exposure(LocalDate.of(2019, 5, 14).toEpochDay().toInt()),
                    Exposure(LocalDate.of(2019, 8, 5).toEpochDay().toInt()),
                    // very recent exposures
                    Exposure(now.minusDays(10).toEpochDay().toInt()),
                    Exposure(now.minusDays(5).toEpochDay().toInt()),
                    Exposure(now.minusDays(12).toEpochDay().toInt())
                )

                state.postValue(RecentExposuresEvent.ExposuresLoadedEvent(mockData))
            }
        }

    }
}
