package cz.covid19cz.erouska.ui.mydata

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import cz.covid19cz.erouska.R
import kotlinx.android.synthetic.main.view_data_item.view.*


class CaseItemView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    init {
        View.inflate(context, R.layout.view_data_item, this)
    }

    var case_title: String? = null
        set(value) {
            field = value
            title_text.text = value
        }

    var case_subtitle: String? = null
        set(value) {
            field = value
            subtitle_text.text = value
        }

    var case_icon: Drawable? = null
        set(value) {
            field = value
            value?.let {
                icon.setImageDrawable(it)
            }
        }
}




