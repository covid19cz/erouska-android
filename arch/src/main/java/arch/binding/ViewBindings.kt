package arch.binding

import android.view.View
import androidx.databinding.BindingAdapter


@BindingAdapter("backgroundResource")
fun setBackgroundResource(view: View, resId: Int) {
    if (resId != 0) {
        view.setBackgroundResource(resId)
    } else {
        view.background = null
    }
}

@BindingAdapter("visibleOrGone")
fun setVisibleOrGone(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("visibleOrInvisible")
fun setVisibleOrInvisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}