package cz.covid19cz.erouska.ui.exposure.entity

data class PreventionData(
    val title: String = "",
    val items: ArrayList<PreventionItem> = arrayListOf()
)