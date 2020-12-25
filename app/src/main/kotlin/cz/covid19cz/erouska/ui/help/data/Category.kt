package cz.covid19cz.erouska.ui.help.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(
    var title: String,
    var subtitle: String,
    var icon: String,
    var questions: List<Question>
) : Parcelable