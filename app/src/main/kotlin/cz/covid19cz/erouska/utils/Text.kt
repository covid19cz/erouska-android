package cz.covid19cz.erouska.utils

import android.content.Context
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import kotlinx.android.parcel.RawValue

sealed class Text {
    data class CharText(val charSequence: CharSequence) : Text()

    data class ResText(
        @StringRes val resId: Int,
        val formatArgs: List<@RawValue Any> = emptyList()
    ) : Text()

    data class ResQuantityText(
        @PluralsRes val resId: Int,
        val quantity: Int,
        val formatArgs: List<@RawValue Any> = emptyList()
    ) : Text()

    data class ResolvedText(val resolver: @RawValue TextResolver): Text()

    fun toCharSequence(context: Context): CharSequence {
        return when (this) {
            is CharText -> charSequence
            is ResText -> if (formatArgs.isEmpty()) context.getText(resId) else context.getString(
                resId,
                *formatArgs.resolverFormatArgs(context).toTypedArray()
            )
            is ResQuantityText -> if (formatArgs.isEmpty()) context.resources.getQuantityString(
                resId, quantity, quantity
            ) else context.resources.getQuantityString(
                resId,
                quantity,
                *formatArgs.resolverFormatArgs(context).toTypedArray()
            )
            is ResolvedText -> resolver(context)
        }
    }
}

private fun List<Any>.resolverFormatArgs(context: Context): List<Any> {
    return this.map { if (it is Text) it.toCharSequence(context) else it }
}

typealias TextResolver = (Context) -> CharSequence

fun CharSequence.toText() = Text.CharText(this)
fun @receiver:StringRes Int.toText(vararg formatArgs: Any) = Text.ResText(this, formatArgs.toList())
fun @receiver:PluralsRes Int.toQuantityText(quantity: Int, vararg formatArgs: Any) =
    Text.ResQuantityText(this, quantity, formatArgs.toList())

@BindingAdapter("android:text")
fun setText(textView: TextView, text: Text?) {
    textView.text = text?.toCharSequence(textView.context)
}

private val PHONE_REGEX = Regex("""(\+\d+)?\s*(\d{3})\s*(\d{3})\s*(\d{3})""")

fun String.formatPhoneNumber(): String {
    val match = PHONE_REGEX.matchEntire(this) ?: return this
    return match.groupValues.drop(1).joinToString(" ")
}
