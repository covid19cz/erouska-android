package cz.covid19cz.erouska.ui.update.legacy.event

sealed class LegacyUpdateEvent {
    object LegacyUpdateExpansion : LegacyUpdateEvent()
    object LegacyUpdateActiveNotification : LegacyUpdateEvent()
    object LegacyUpdatePhoneNumbers : LegacyUpdateEvent()
    object LegacyUpdatePrivacy : LegacyUpdateEvent()
    object LegacyUpdateFinish : LegacyUpdateEvent()
}
