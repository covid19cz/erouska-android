package cz.covid19cz.erouska.ui.update

import androidx.lifecycle.MutableLiveData
import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.update.event.LegacyUpdateEvent

class LegacyUpdateVM(
    private val sharedPrefsRepository: SharedPrefsRepository
) : BaseArchViewModel() {

    val state = MutableLiveData<LegacyUpdateEvent>(LegacyUpdateEvent.LegacyUpdateExpansion)

    fun next() {
        val next = when (state.value) {
            LegacyUpdateEvent.LegacyUpdateExpansion -> LegacyUpdateEvent.LegacyUpdateActiveNotification
            LegacyUpdateEvent.LegacyUpdateActiveNotification -> LegacyUpdateEvent.LegacyUpdatePhoneNumbers
            LegacyUpdateEvent.LegacyUpdatePhoneNumbers -> LegacyUpdateEvent.LegacyUpdatePrivacy
            else -> LegacyUpdateEvent.LegacyUpdateFinish
        }

        state.value = next
    }

    fun previous() {
        val prev = when (state.value) {
            LegacyUpdateEvent.LegacyUpdatePrivacy -> LegacyUpdateEvent.LegacyUpdatePhoneNumbers
            LegacyUpdateEvent.LegacyUpdatePhoneNumbers -> LegacyUpdateEvent.LegacyUpdateActiveNotification
            else -> LegacyUpdateEvent.LegacyUpdateExpansion
        }

        state.value = prev
    }

    fun finish() {
        sharedPrefsRepository.markUpdateFromLegacyVersionCompleted()
    }

}