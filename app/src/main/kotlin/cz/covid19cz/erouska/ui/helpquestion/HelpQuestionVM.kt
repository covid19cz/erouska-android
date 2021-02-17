package cz.covid19cz.erouska.ui.helpquestion

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import arch.adapter.RecyclerLayoutStrategy
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.data.Question

class HelpQuestionVM @ViewModelInject constructor() : BaseVM() {

    val layoutStrategy = object : RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return R.layout.item_help_question
        }
    }

    var items = ObservableArrayList<Question>()

}
