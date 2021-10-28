package cz.covid19cz.erouska.ui.ragnarok

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.viewModelScope
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.base.UrlEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RagnarokVM @Inject constructor(private val enRepository : ExposureNotificationsRepository) : BaseVM() {

    val headline = MutableLiveData(AppConfig.ragnarokHeadline)
    val body = MutableLiveData(AppConfig.ragnarokBody)

    fun showMoreInfo(){
        publish(UrlEvent(AppConfig.ragnarokMoreInfo))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){
        viewModelScope.launch {
            kotlin.runCatching {
                enRepository.stop()
            }
        }
    }
}