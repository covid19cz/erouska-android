package cz.covid19cz.app.ui.about.entity

import androidx.databinding.ObservableArrayList
import com.google.gson.annotations.SerializedName

data class AboutRoleItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val title: String,
    @SerializedName("people")
    val items : ObservableArrayList<AboutProfileItem>
)