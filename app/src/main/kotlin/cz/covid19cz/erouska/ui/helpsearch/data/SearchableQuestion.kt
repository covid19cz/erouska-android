package cz.covid19cz.erouska.ui.helpsearch.data

data class SearchableQuestion(
    val category: String,
    var question: String = "",
    var answer: String = ""
)