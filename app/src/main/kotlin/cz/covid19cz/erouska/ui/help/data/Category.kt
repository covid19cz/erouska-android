package cz.covid19cz.erouska.ui.help.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(
    val type: Type,
    val title: String,
    val subtitle: String,
    val icon: String,
    val questions: List<Question>
) : Parcelable, Comparable<Category> {

    enum class Type {
        ABOUT, FAQ, HOW_IT_WORKS
    }

    override fun compareTo(other: Category): Int {
        return type.ordinal.compareTo(other.type.ordinal)
    }

}
