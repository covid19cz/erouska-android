package arch.binding

import android.content.res.ColorStateList
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter


@BindingAdapter(value = ["progressTintResource"], requireAll = false)
fun setImageResourceCircular(view: ProgressBar, resId: Int) {
   view.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, resId))
}


