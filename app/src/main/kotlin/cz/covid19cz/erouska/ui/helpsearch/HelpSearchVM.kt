package cz.covid19cz.erouska.ui.helpsearch

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.viewModelScope
import arch.adapter.RecyclerLayoutStrategy
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.data.FaqCategory
import cz.covid19cz.erouska.ui.help.data.toFaqCategories
import cz.covid19cz.erouska.ui.helpsearch.data.SearchableQuestion
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.Markdown
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class HelpSearchVM @Inject constructor(
    val markdown: Markdown
) : BaseVM() {

    val layoutStrategy = object : RecyclerLayoutStrategy {
        override fun getLayoutId(item: Any): Int {
            return R.layout.item_search
        }
    }

    val searchResult = ObservableArrayList<SearchableQuestion>()
    val searchEmpty = SafeMutableLiveData(searchResult.isEmpty())
    val content = ArrayList<SearchableQuestion>()
    val queryData = SafeMutableLiveData("")
    val minQueryLength = 2

    private var searchJob: Job? = null

    fun fillQuestions() {
        val structuredQs: List<FaqCategory> = AppConfig.helpJson.toFaqCategories()

        val allQuestions = structuredQs.map {
            it.questions.map { question ->
                SearchableQuestion(it.title, question.question, question.answer)
            }
        }.flatten()

        content.clear()
        content.addAll(allQuestions)
    }

    fun searchQuery(query: String?) {
        searchJob?.cancel()
        this.queryData.value = query?.trim() ?: ""

        if (queryData.value.length >= minQueryLength) {
            searchJob = viewModelScope.launch {
                startSearch()
            }
        } else {
            resetSearchResult()
        }
    }

    private suspend fun updateSearchResult(searchResults: List<SearchableQuestion>) =
        withContext(Dispatchers.Main) {
            searchResult.clear()
            searchResult.addAll(searchResults)
            updateSearchResultCount()
        }

    private fun resetSearchResult() {
        searchResult.clear()
        updateSearchResultCount()
    }

    private fun updateSearchResultCount() {
        searchEmpty.postValue(searchResult.isEmpty())
    }

    private suspend fun startSearch() = withContext(Dispatchers.Default) {
        val result = searchQueryInText()
        if (isActive) {
            updateSearchResult(result)
        } else {
            L.d("Job was already cancelled, not going to display results")
        }
    }

    private fun searchQueryInText(): List<SearchableQuestion> {
        val tempSearchResult = mutableListOf<SearchableQuestion>()
        content.forEach { question ->
            val newQ = highlightSearchedText(question.question)
            val newA = highlightSearchedText(question.answer)

            if (newQ.second || newA.second) {
                val q = SearchableQuestion(question.category)
                q.answer = newA.first
                q.question = newQ.first
                tempSearchResult.add(q)
            }
        }
        return tempSearchResult
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
