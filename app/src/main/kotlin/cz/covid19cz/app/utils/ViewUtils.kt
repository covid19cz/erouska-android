package cz.covid19cz.app.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import kotlin.math.roundToInt

fun Context.dpToPx(dp: Int): Int {
    val displayMetrics = getResources().getDisplayMetrics()
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT.toFloat())).roundToInt()
}

fun EditText.setOnDoneListener(onDone: ()->Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onDone()
            return@setOnEditorActionListener true
        }
        return@setOnEditorActionListener false
    }
}
