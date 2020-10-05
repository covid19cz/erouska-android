package cz.covid19cz.erouska.utils

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.annotation.NonNull
import dagger.hilt.android.qualifiers.ApplicationContext
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import org.commonmark.ext.autolink.AutolinkExtension
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
                override fun configureTheme(@NonNull builder: MarkwonTheme.Builder) {
                    builder
                        .codeBlockMargin(0)
                        .codeTextColor(Color.BLACK)
                        .codeBlockTextColor(Color.BLACK)
                        .codeBackgroundColor(Color.YELLOW)
                        .codeBlockBackgroundColor(Color.YELLOW)
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
