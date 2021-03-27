package cz.covid19cz.erouska.ui.dashboard

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import cz.covid19cz.erouska.R
import kotlinx.android.synthetic.main.view_data_item.view.*

class TravellerDashboardCardView : DashboardCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    override fun getLayoutId(): Int = R.layout.traveller_dashboard_card_view

    override var card_icon: Drawable? = null
        set(value) {
            field = value
            value?.let {
                icon.setImageDrawable(it)
            }
        }

}
