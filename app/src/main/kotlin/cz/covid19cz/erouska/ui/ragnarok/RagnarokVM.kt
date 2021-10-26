package cz.covid19cz.erouska.ui.ragnarok

import androidx.lifecycle.MutableLiveData
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.base.UrlEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RagnarokVM @Inject constructor() : BaseVM() {

    val headline = MutableLiveData(AppConfig.ragnarokHeadline)
    val body = MutableLiveData(AppConfig.ragnarokBody)

    fun showMoreInfo(){
        publish(UrlEvent(AppConfig.ragnarokMoreInfo))
    }
}