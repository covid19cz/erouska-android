package cz.covid19cz.app.ui.about.entity

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