package cz.covid19cz.erouska.ui.about.entity

import com.google.gson.annotations.SerializedName

class AboutProfileItem(
    @SerializedName("name")
    val name : String,
    @SerializedName("surname")
    val surname : String,
    @SerializedName("photo")
    val photoUrl : String,
    @SerializedName("linkedin")
    val linkedin : String ) {
}