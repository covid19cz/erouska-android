package arch.binding

import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("errorResource")
fun TextInputLayout.setErrorResource(resId: Int?) {
    error = resId?.let {
        context.getString(resId)
    }
}