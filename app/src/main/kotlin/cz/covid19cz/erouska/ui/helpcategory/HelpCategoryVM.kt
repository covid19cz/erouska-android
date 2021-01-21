package cz.covid19cz.erouska.ui.helpcategory

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import arch.adapter.RecyclerLayoutStrategy
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.data.Question
import cz.covid19cz.erouska.ui.helpquestion.HelpQuestionFragmentArgs

class HelpCategoryVM @ViewModelInject constructor() : BaseVM() {

    lateinit var categoryTitle: String

    val layoutStrategy = object : RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return R.layout.item_help_question
        }
    }

    var items = ObservableArrayList<Question>()

    fun onItemClicked(question: Question) {
        navigate(R.id.nav_help_question, HelpQuestionFragmentArgs(question = question, category = categoryTitle).toBundle())
    }

    fun fillInQuestions(questions: List<Question>) {
        items.addAll(questions)
    }

    fun onSearchTapped() = navigate(R.id.nav_help_search)

}
