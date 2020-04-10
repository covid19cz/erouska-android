package cz.covid19cz.erouska.utils

import android.widget.TextView
import cz.covid19cz.erouska.App
import io.noties.markwon.Markwon
import io.noties.markwon.image.glide.GlideImagesPlugin

object Markdown {
    private val markwon by lazy {
        Markwon.builder(App.instance)
            .usePlugin(GlideImagesPlugin.create(App.instance))
            .build()
    }

    fun show(textView: TextView, markdown: String?) {
       markdown?.replace("\\n", "\n")?.let {
            markwon.setMarkdown(textView, it)
        }
    }
}
