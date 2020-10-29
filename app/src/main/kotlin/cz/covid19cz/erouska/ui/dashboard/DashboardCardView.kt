package cz.covid19cz.erouska.ui.dashboard

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show
import kotlinx.android.synthetic.main.dashboard_card_view.view.*
import kotlinx.android.synthetic.main.view_data_item.view.subtitle_text
import kotlinx.android.synthetic.main.view_data_item.view.title_text

class DashboardCardView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    init {
        View.inflate(context, R.layout.dashboard_card_view, this)
    }

    var card_title: String? = null
        set(value) {
            field = value
            title_text.text = value
        }


    var card_subtitle: String? = null
        set(value) {
            field = value
            subtitle_text.text = value
            subtitle_text.show()
        }

    var card_button_text: String? = null
        set(value) {
            field = value
            button.text = value
            button.show()
        }

    var card_show_right_arrow: Boolean? = false
        set(value) {
            field = value
            if (value == true) {
                title_text.setCompoundDrawablesWithIntrinsicBounds(card_icon, null, ContextCompat.getDrawable(context, R.drawable.ic_arrow_right), null)
            } else {
                title_text.setCompoundDrawablesWithIntrinsicBounds(card_icon, null, null, null)
            }
        }

    var card_on_content_click: OnClickListener? = null
        set(value) {
            field = value
            content_container.setOnClickListener(value)
            content_container.isFocusable = true
            content_container.isClickable = true
        }

    var card_actionable_button: Boolean? = false
        set(value) {
            field = value
            if (value == true) {
                button.show()
            } else {
                button.hide()
            }
        }

    var card_has_content: Boolean? = true
        set(value) {
            field = value
            if (value == true) {
                subtitle_text.show()
            } else {
                subtitle_text.hide()
            }
        }

    var card_alert: Boolean? = false
        set(value) {
            field = value
            if (value == true) {
                title_text.setTextColor(ContextCompat.getColor(context, R.color.exposure_notification_red))
            } else {
                title_text.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary))
            }
        }

    var card_icon: Drawable? = null
        set(value) {
            field = value
            value?.let {
                title_text.setCompoundDrawablesWithIntrinsicBounds(it, null, if (card_show_right_arrow == true) ContextCompat.getDrawable(context, R.drawable.ic_arrow_right) else null , null)
            }
        }
}




