package cz.covid19cz.erouska.ui.dashboard

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cz.covid19cz.erouska.R

data class DashboardCard(

    val type: Type,
    var title: String = "",
    var subtitle: String = "",
    var isVisible: Boolean = type.isVisible

) {
    @DrawableRes
    val icon: Int = type.icon

    @StringRes
    val buttonText: Int = type.buttonText
    val actionableButton: Boolean = type.actionableButton
    val actionableContent: Boolean = type.actionableContent
    val isAlert: Boolean = type.isAlert
    val hasContent: Boolean = type.hasContent

}

enum class Type(
    @StringRes
    val title: Int,
    @StringRes
    val subtitle: Int,
    @DrawableRes
    val icon: Int,
    @StringRes
    val buttonText: Int,
    val actionableButton: Boolean,
    val actionableContent: Boolean,
    val hasContent: Boolean,
    val isAlert: Boolean,
    val isVisible: Boolean
) {
    EN_API(
        R.string.exposure_notifications_off_title,
        R.string.exposure_notifications_off_body,
        R.drawable.ic_error,
        R.string.enable,
        true,
        false,
        true,
        true,
        false
    ),
    BLUETOOTH(
        R.string.bt_disabled_title,
        R.string.bt_disabled_desc,
        R.drawable.ic_off_bluetooth,
        R.string.enable_bluetooth_button,
        true,
        false,
        true,
        true,
        false
    ),
    LOCATION_SERVICES(
        R.string.location_off_header,
        R.string.location_off_body,
        R.drawable.ic_off_location,
        R.string.location_off_turn_on,
        true,
        false,
        true,
        true,
        false
    ),
    ACTIVE_APP(
        R.string.dashboard_title_running,
        R.string.dashboard_body,
        R.drawable.ic_active,
        R.string.pause_app,
        true,
        false,
        false,
        false,
        false
    ),
    INACTIVE_APP(
        R.string.dashboard_title_paused,
        R.string.dashboard_body_paused,
        R.drawable.ic_pause,
        R.string.start_app,
        true,
        false,
        false,
        false,
        false
    ),
    RISKY_ENCOUNTER(
        R.string.empty,
        R.string.empty,
        R.drawable.ic_risky_encounter,
        R.string.empty,
        false,
        true,
        false,
        true,
        false
    ),
    NO_RISKY_ENCOUNTER(
        R.string.empty,
        R.string.empty,
        R.drawable.ic_no_risky_encounter,
        R.string.empty,
        false,
        true,
        false,
        false,
        false
    ),
    POSITIVE_TEST(
        R.string.dashboard_positive_test_title,
        R.string.empty,
        R.drawable.ic_positive,
        R.string.empty,
        true,
        false,
        false,
        false,
        true
    ),
    TRAVEL(
        R.string.dashboard_travel_title,
        R.string.empty,
        R.drawable.ic_travel,
        R.string.empty,
        false,
        true,
        true,
        false,
        false
    )
}
