package cz.covid19cz.erouska.ui.help.data

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize
import java.lang.reflect.Type
import java.util.*

@Parcelize
data class FaqCategory(
    val title: String,
    val subtitle: String,
    val icon: String,
    val questions: List<Question>
) : Category, Parcelable

fun String?.toFaqCategories(): List<FaqCategory> {
    val categoryType: Type = object : TypeToken<ArrayList<FaqCategory>>() {}.type
    return try {
        val structuredQs: ArrayList<FaqCategory>? = Gson().fromJson(this, categoryType)
        structuredQs.orEmpty()
    } catch (ex1: JsonParseException) {
        Collections.emptyList()
    }
}

