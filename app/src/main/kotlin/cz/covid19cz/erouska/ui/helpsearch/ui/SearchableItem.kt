package cz.covid19cz.erouska.ui.helpsearch.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.utils.Markdown
import kotlinx.android.synthetic.main.item_search_layout.view.*

class SearchableItem : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    init {
        View.inflate(context, R.layout.item_search_layout, this)
    }

    var markdown: Markdown? = null

    var search_category: String? = null
        set(value) {
            field = value
            category.text = value
        }

    var search_question: String? = null
        set(value) {
            value?.let {
                value?.let {
                    markdown?.show(question, it)
                }
                field = value
            }
        }

    var search_answer: String? = null
        set(value) {
            value?.let {
                markdown?.show(answer, it)
            }
            field = value
        }

}
