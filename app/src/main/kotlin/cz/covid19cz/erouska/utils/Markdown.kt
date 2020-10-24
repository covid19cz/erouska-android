package cz.covid19cz.erouska.utils

import android.content.Context
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import androidx.annotation.Nullable
import cz.covid19cz.erouska.R
import dagger.hilt.android.qualifiers.ApplicationContext
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.inlineparser.InlineProcessor
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.inlineparser.OpenBracketInlineProcessor
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.node.CustomNode
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Markdown @Inject constructor(
    @ApplicationContext val context: Context
) {
    private val markwon by lazy {
        Markwon.builder(context)
            .usePlugin(HtmlPlugin.create { plugin -> plugin.excludeDefaults(false) })
            .usePlugin(GlideImagesPlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureParser(builder: Parser.Builder) {
                    builder.extensions(listOf(AutolinkExtension.create()))
                }
            })
            .usePlugin(MarkwonInlineParserPlugin.create { factoryBuilder ->
                factoryBuilder
                    .addInlineProcessor(SearchedTextInlineProcessor())
                    .excludeInlineProcessor(OpenBracketInlineProcessor::class.java)
            })
            .usePlugin(object : AbstractMarkwonPlugin() {

                // general formatting
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder.headingBreakHeight(0)
                    builder.headingTextSizeMultipliers(
                        floatArrayOf(1.15f, 1.10f, 1.05f, 1f, .83f, .67f)
                    )
                }

                // searched text highlighting
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    builder.on(SearchedTextNode::class.java, GenericInlineNodeVisitor())
                }

                // searched text highlighting
                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    builder
                        .setFactory(
                            SearchedTextNode::class.java
                        ) { _, _ ->
                            arrayOf(BackgroundColorSpan(context.getColor(R.color.green)))
                        }
                }
            })
            .build()
    }

    fun show(textView: TextView, markdown: String?) {
        markdown?.replace("\\n", "\n")?.let {
            markwon.setMarkdown(textView, it)
        }
    }

    class GenericInlineNodeVisitor : MarkwonVisitor.NodeVisitor<Node?> {

        override fun visit(visitor: MarkwonVisitor, n: Node) {
            val length = visitor.length()
            visitor.visitChildren(n)
            visitor.setSpansForNodeOptional(n, length)
        }
    }

    class SearchedTextInlineProcessor : InlineProcessor() {

        override fun specialCharacter(): Char {
            return '['
        }

        @Nullable
        override fun parse(): Node? {
            val match = match(RE);
            if (match != null) {
                // consume syntax
                val text = match.substring(2, match.length - 2);
                val node = SearchedTextNode()
                node.appendChild(text(text))
                return node
            }
            return null
        }

        companion object {
            private val RE: Pattern = Pattern.compile("\\[\\[(.+?)\\]\\]")
        }
    }

    private class SearchedTextNode : CustomNode()

}
