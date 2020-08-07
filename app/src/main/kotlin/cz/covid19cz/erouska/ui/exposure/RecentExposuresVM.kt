package cz.covid19cz.erouska.ui.exposure

import androidx.lifecycle.viewModelScope
import arch.event.SingleLiveEvent
import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.ui.exposure.event.RecentExposuresEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecentExposuresVM : BaseArchViewModel() {

    val state = SingleLiveEvent<RecentExposuresEvent>()

    fun loadExposures() {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO Replace with loading of real exposure data, remove this mock
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
