package arch.binding

import android.webkit.WebView
import androidx.databinding.BindingAdapter


@BindingAdapter("url")
fun setUrl(view: WebView, url: String?) {
    url?.let {
        view.loadUrl(it)
    }
}

@BindingAdapter("java_script_enabled")
fun setJavScriptEnabled(view: WebView, enabled: Boolean) {
    view.settings.javaScriptEnabled = enabled
}