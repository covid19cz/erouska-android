package cz.covid19cz.erouska.ui.success

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_success.*

abstract class SuccessFragment :
    BaseFragment<FragmentHelpBinding, SuccessVM>(R.layout.fragment_success, SuccessVM::class) {

    abstract val title: Int
    open val description: Int? = null
    open fun onClose() {
        navController().popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)
        success_title.setText(title)
        description?.let { success_description.setText(it) }
        close_button.setOnClickListener {
            onClose()
        }
    }
}
