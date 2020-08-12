package cz.covid19cz.erouska.ui.about.entity

import androidx.databinding.ObservableArrayList
import com.google.gson.annotations.SerializedName

data class AboutRoleItem(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val title: String?,
    @SerializedName("people")
    val items : ObservableArrayList<AboutProfileItem>?
)