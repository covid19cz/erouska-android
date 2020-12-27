package cz.covid19cz.erouska.ui.helpsearch

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import arch.adapter.RecyclerLayoutStrategy
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.data.Category
import cz.covid19cz.erouska.ui.help.data.Question
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent
import cz.covid19cz.erouska.ui.helpsearch.data.SearchableQuestion
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.Markdown
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern

class HelpSearchVM @ViewModelInject constructor(
    val markdown: Markdown
) : BaseVM() {

    val layoutStrategy = object : RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return R.layout.item_search
        }
    }

    val searchResult = ObservableArrayList<SearchableQuestion>()
    val content = ArrayList<SearchableQuestion>()
    val queryData = SafeMutableLiveData("")

    private var searchJob: Job? = null

    fun fillQuestions() {
        val structuredQs = listOf(
            Category(
                type = Category.Type.FAQ,
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
                type = Category.Type.FAQ,
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

        val allQuestions = structuredQs.map {
            it.questions.map { question ->
                SearchableQuestion(it.title, question.question, question.answer)
            }
        }.flatten()

        content.clear()
        content.addAll(allQuestions)
    }

    fun goBack() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.GO_BACK))
    }

    fun searchQuery(query: String?) {

        searchJob?.cancel()

        this.queryData.value = query?.trim() ?: ""

        if (queryData.value.length >= 2) {
            searchJob = viewModelScope.launch {
                try {
                    searchQueryInText()
                } catch (cancelException: CancellationException) {
                    L.d("Job cancelled")
                }
            }
        } else {
            resetSearch()
        }

    }

    fun resetSearch() {
        searchResult.clear()
    }

    private fun searchQueryInText() {
        searchResult.clear()
        content.forEach { question ->

            val newQ = highlightSearchedText(question.question)
            val newA = highlightSearchedText(question.answer)

            if (newQ.second || newA.second) {
                val q = SearchableQuestion(question.category)
                q.answer = newA.first
                q.question = newQ.first
                searchResult.add(q)
                L.i("adding to search result:$q")
            }
        }
    }

    private fun highlightSearchedText(originalText: String): Pair<String, Boolean> {
        val pattern = StringUtils.stripAccents(queryData.value)
        val r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        val searchedText = StringUtils.stripAccents(originalText)
        var printedText = originalText

        val m = r.matcher(searchedText)

        val replaceList = arrayListOf<String>()
        while (m.find()) {
            replaceList.add(printedText.substring(m.start(0), m.end(0)))
        }
        val searchMatches = replaceList.distinct()

        for (replaceString in searchMatches) {
            printedText = printedText.replace(
                replaceString,
                "${Markdown.doubleSearchChar}${replaceString}${Markdown.doubleSearchChar}"
            )
        }

        return Pair(printedText, searchMatches.isNotEmpty())
    }

}
