package cz.covid19cz.erouska.utils

import android.content.Context
import android.widget.TextView
import io.noties.markwon.Markwon
import io.noties.markwon.image.glide.GlideImagesPlugin

class Markdown(val context: Context) {
    private val markwon by lazy {
        Markwon.builder(context)
            .usePlugin(GlideImagesPlugin.create(context))
            .build()
    }

    fun show(textView: TextView, markdown: String?) {
       markdown?.replace("\\n", "\n")?.let {
            markwon.setMarkdown(textView, it)
        }
    }
}
