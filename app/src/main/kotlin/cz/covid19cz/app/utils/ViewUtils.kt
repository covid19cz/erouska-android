package cz.covid19cz.app.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent.Builder
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import cz.covid19cz.app.R
import kotlin.math.roundToInt

fun Context.dpToPx(dp: Int): Int {
    val displayMetrics = getResources().getDisplayMetrics()
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT.toFloat())).roundToInt()
}

fun EditText.setOnDoneListener(onDone: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onDone()
            return@setOnEditorActionListener true
        }
        return@setOnEditorActionListener false
    }
}

fun View.focusAndShowKeyboard() {
    /**
     * This is to be called when the window already has focus.
     */
    fun View.showTheKeyboardNow() {
        if (isFocused) {
            post {
                // We still post the call, just in case we are being notified of the windows focus
                // but InputMethodManager didn't get properly setup yet.
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        // No need to wait for the window to get focus.
        showTheKeyboardNow()
    } else {
        // We need to wait until the window gets focus.
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    // This notification will arrive just before the InputMethodManager gets set up.
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        // It’s very important to remove this listener once we are done.
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

fun View.hideKeyboard() {
    val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    im.hideSoftInputFromWindow(this.windowToken, 0);
}

fun Context.makeCall(phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
    startActivity(this, intent, null)
}

fun Context.sendEmail(subject: String, toAddress: String, text: String? = null, ccAddress: String? = null) {
    L.i("Send email")
    val to = arrayOf(toAddress)
    val emailIntent = Intent(Intent.ACTION_SEND)

    emailIntent.data = Uri.parse("mailto:")
    emailIntent.type = "message/rfc822"
    emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    text?.let {
        emailIntent.putExtra(Intent.EXTRA_TEXT, text)
    }

    ccAddress?.let {
        val cc = arrayOf(it)
        emailIntent.putExtra(Intent.EXTRA_CC, cc)
    }
    try {
        startActivity(this, Intent.createChooser(emailIntent, getString(R.string.send_email_chooser)), null)
        L.i("Finished sending email...")
    } catch (ex: ActivityNotFoundException) {
        val th = this
        Toast.makeText(
            this,
            getString(R.string.no_emal_client), Toast.LENGTH_SHORT
        ).show()
    }
}

fun Context.openChromeTab(url: String) {
    val builder = Builder()
    builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(this, Uri.parse(url))
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}
