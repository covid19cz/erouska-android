package cz.covid19cz.erouska.utils

import android.content.Context
import android.graphics.Color
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import dagger.hilt.android.qualifiers.ApplicationContext
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.node.StrongEmphasis
import org.commonmark.parser.Parser
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Markdown @Inject constructor(@ApplicationContext val context: Context) {
    private val markwon by lazy {
        Markwon.builder(context)
            .usePlugin(HtmlPlugin.create { plugin -> plugin.excludeDefaults(false) })
            .usePlugin(GlideImagesPlugin.create(context))
            .usePlugin(object: AbstractMarkwonPlugin() {
                override fun configureParser(builder: Parser.Builder) {
                    builder.extensions(listOf(AutolinkExtension.create()))
                }
            })
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    builder
                        .setFactory(
                            StrongEmphasis::class.java
                        ) { configuration, props -> arrayOf(
                            BackgroundColorSpan(Color.GREEN)
                        ) }
                }
            })
            .build()
    }

    fun show(textView: TextView, markdown: String?) {
       markdown?.replace("\\n", "\n")?.let {
            markwon.setMarkdown(textView, it)
        }
    }
}
