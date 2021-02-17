package cz.covid19cz.erouska.ui.help.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Question
constructor(
    var question: String,
    var answer: String
) : Parcelable