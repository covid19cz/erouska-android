package cz.covid19cz.erouska.ui.help

import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import arch.adapter.RecyclerLayoutStrategy
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.exposurehelp.ExposureHelpFragmentArgs
import cz.covid19cz.erouska.ui.exposurehelp.entity.ExposureHelpType
import cz.covid19cz.erouska.ui.help.data.Category
import cz.covid19cz.erouska.ui.help.data.Question
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent
import cz.covid19cz.erouska.ui.helpcategory.HelpCategoryFragmentArgs
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.Markdown
import kotlinx.coroutines.Job
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern

class HelpVM @ViewModelInject constructor() : BaseVM() {

    val layoutStrategy = object : RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return R.layout.item_help_category
        }
    }

    var items = ObservableArrayList<Category>()

    val searchControlsEnabled = SafeMutableLiveData(false)
    val searchResultCount = SafeMutableLiveData(0)
    val content = SafeMutableLiveData(AppConfig.helpMarkdown)
    val lastMarkedIndex = SafeMutableLiveData(0)
    val queryData = SafeMutableLiveData("")
    lateinit var displayedText: String
    private var searchMatches: List<String> = arrayListOf()

    private var searchJob: Job? = null

    fun fillInHelp() = items.addAll(
        listOf(
            Category(
                title = "Fungování eRoušky",
                subtitle = "sběr a vyhodnocení dat, význam upozornění",
                icon = "https://erouska.cz/img/symptoms/ic_temperature.png",
                questions = listOf(
                    Question(
                        "Jak eRouška zaznamenává a zpracovává data o setkáních uživatelů?",
                        "Chytrý telefon s aplikací eRouška zaznamená přes Bluetooth LE anonymní identifikátory (ID) z jiných zařízení s touto aplikací. Informaci o „setkání“ a jeho délce ukládá do své vnitřní paměti."
                    ),
                    Question(
                        "Jak eRouška vyhodnocuje rizikové setkání?",
                        "Epidemiologové stanovují rizikový kontakt jako setkání, které je ve vzdálenosti bližší než 2 metry po dobu alespoň 15 minut. Aplikace eRouška se to snaží co nejpřesněji změřit dostupnými technologiemi. Vzdálenost mezi uživateli, respektive jejich telefony, se odhaduje na základě síly signálu Bluetooth. Doba setkání se posuzuje podle měřicích oken – telefon v několikaminutových intervalech zjišťuje, zda jsou v okolí jiné telefony s eRouškou."
                    )
                )
            ),
            Category(
                title = "Instalace a kompatibilita",
                subtitle = "podporované mobily a možnosti instalace",
                icon = "https://erouska.cz/img/symptoms/ic_temperature.png",
                questions = listOf(
                    Question(
                        "Odkud si můžu eRoušku bezpečně stáhnout a nainstalovat?",
                        "Aplikace eRouška je dostupná pouze v Obchodě Play pro Android a v App Store pro iPhone."
                    ),
                    Question(
                        "Jak eRouška vyhodnocuje rizikové setkání?",
                        "Epidemiologové stanovují rizikový kontakt jako setkání, které je ve vzdálenosti bližší než 2 metry po dobu alespoň 15 minut. Aplikace eRouška se to snaží co nejpřesněji změřit dostupnými technologiemi. Vzdálenost mezi uživateli, respektive jejich telefony, se odhaduje na základě síly signálu Bluetooth. Doba setkání se posuzuje podle měřicích oken – telefon v několikaminutových intervalech zjišťuje, zda jsou v okolí jiné telefony s eRouškou."
                    )
                )
            )
        )
    )

    fun onItemClicked(category: Category) {
        L.i("clicked category $category")
        navigate(R.id.nav_help_category, HelpCategoryFragmentArgs(category = category).toBundle())
    }

    fun goBack() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.GO_BACK))
    }

    fun openChatBot() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.OPEN_CHATBOT))
    }

    fun searchQuery(query: String?) {
//        searchJob?.cancel()
//
//        this.queryData.value = query?.trim() ?: ""
//
//        if (queryData.value.length >= 2) {
//            searchJob = viewModelScope.launch {
//                try {
//                    searchQueryInText()
//                } catch (cancelException: CancellationException) {
//                    L.d("Job cancelled")
//                }
//            }
//        } else {
//            resetSearch()
//        }

    }

    private fun searchQueryInText() {
        val pattern = StringUtils.stripAccents(queryData.value)
        val r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        val searchedText = StringUtils.stripAccents(AppConfig.helpMarkdown)
        var printedText = AppConfig.helpMarkdown

        val m = r.matcher(searchedText)

        val replaceList = arrayListOf<String>()
        while (m.find()) {
            replaceList.add(printedText.substring(m.start(0), m.end(0)))
        }
        searchResultCount.value = replaceList.size
        searchMatches = replaceList.distinct()

        for (replaceString in searchMatches) {
            printedText = printedText.replace(
                replaceString,
                "${Markdown.doubleSearchChar}${replaceString}${Markdown.doubleSearchChar}"
            )
        }

        content.value = printedText
        searchControlsEnabled.value = replaceList.isNotEmpty()
        // we don't want to take into account the currently search query length
        findPositionOfNextResult(0)
    }

    fun resetSearch() {
        searchControlsEnabled.value = false
        content.value = AppConfig.helpMarkdown
        searchResultCount.value = 0
        lastMarkedIndex.value = 0
    }

    fun findPositionOfPreviousResult() {
        if (searchResultCount.value <= 0) {
            return
        }

        val searchedText = displayedText.substring(0, lastMarkedIndex.value)
        var index = findIndexOfPreviousResult(searchedText)
        if (index == -1) {
            // the search match was not found, let's search in the whole text (from the end)
            index = findIndexOfPreviousResult(displayedText)
        }

        lastMarkedIndex.value = index
    }

    private fun findIndexOfPreviousResult(searchableText: String): Int {
        return searchableText.lastIndexOfAny(searchMatches, ignoreCase = true)
    }

    fun findPositionOfNextResult(overrideQueryDataLength: Int? = null) {
        if (searchResultCount.value <= 0) {
            return
        }

        var index = findIndexOfNextResult(
            lastMarkedIndex.value + (overrideQueryDataLength ?: queryData.value.length)
        )
        if (index == -1) {
            // the search match was not found, let's search from the beginning
            index = findIndexOfNextResult(0)
        }
        lastMarkedIndex.value = index
    }

    private fun findIndexOfNextResult(startIndex: Int): Int {
        return displayedText.indexOfAny(searchMatches, startIndex, ignoreCase = true)
    }

}
