package cz.covid19cz.erouska.ui.about.entity

import androidx.databinding.ObservableArrayList
import com.google.gson.annotations.SerializedName
import cz.covid19cz.erouska.ui.about.entity.AboutProfileItem

data class AboutRoleItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val title: String,
    @SerializedName("people")
    val items : ObservableArrayList<AboutProfileItem>
)