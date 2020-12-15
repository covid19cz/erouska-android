package cz.covid19cz.erouska.ext

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.attachKeyboardController() {
    setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            showKeyboard()
        } else {
            hideKeyboard()
        }
    }
}

fun View.showKeyboard() {
    post {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.hideKeyboard() {
    post {
        val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}