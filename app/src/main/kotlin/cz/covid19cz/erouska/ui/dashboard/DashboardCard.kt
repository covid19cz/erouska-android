package cz.covid19cz.erouska.ui.dashboard

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import cz.covid19cz.erouska.R

data class DashboardCard(

    val type: Type,
    var title: MutableLiveData<String> = MutableLiveData(""),
    var subtitle: MutableLiveData<String> = MutableLiveData(""),
    var icon: MutableLiveData<Drawable> = MutableLiveData()

) : Comparable<DashboardCard> {

    @StringRes
    val buttonText: Int = type.buttonText
    var actionableButton: MutableLiveData<Boolean> = MutableLiveData(type.actionableButton)
    val actionableContent: MutableLiveData<Boolean> = MutableLiveData(type.actionableContent)
    val isAlert: MutableLiveData<Boolean> = MutableLiveData(type.isAlert)
    val hasContent: Boolean = type.hasContent

    override fun compareTo(other: DashboardCard): Int {
        return type.compareTo(other.type)
    }

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
    val isAlert: Boolean
) {

    BLUETOOTH(
        R.string.bt_disabled_title,
        R.string.bt_disabled_desc,
        R.drawable.ic_off_bluetooth,
        R.string.enable_bluetooth_button,
        true,
        false,
        true,
        true
    ),
    LOCATION_SERVICES(
        R.string.location_off_header,
        R.string.location_off_body,
        R.drawable.ic_off_location,
        R.string.location_off_turn_on,
        true,
        false,
        true,
        true
    ),
    ACTIVE_APP(
        R.string.dashboard_title_running,
        R.string.dashboard_body,
        R.drawable.ic_active,
        R.string.pause_app,
        true,
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
        true
    ),
    POSITIVE_TEST(
        R.string.dashboard_positive_test_title,
        R.string.empty,
        R.drawable.ic_positive,
        R.string.dashboard_positive_test_button,
        true,
        true,
        false,
        false
    ),
    TRAVEL(
        R.string.dashboard_travel_title,
        R.string.empty,
        R.drawable.ic_travel,
        R.string.empty,
        false,
        true,
        true,
        false
    )
}
