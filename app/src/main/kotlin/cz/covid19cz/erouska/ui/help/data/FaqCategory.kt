package cz.covid19cz.erouska.ui.help.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaqCategory(
    val title: String,
    val subtitle: String,
    val icon: String,
    val questions: List<Question>
) : Category, Parcelable
