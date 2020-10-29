package cz.covid19cz.erouska.ui.help

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent
import cz.covid19cz.erouska.utils.L
import cz.covid19cz.erouska.utils.Markdown
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern

class HelpVM @ViewModelInject constructor() : BaseVM() {

    val searchControlsEnabled = SafeMutableLiveData(false)
    val searchResultCount = SafeMutableLiveData(0)
    val content = SafeMutableLiveData(AppConfig.helpMarkdown)
    val lastMarkedIndex = SafeMutableLiveData(0)
    val queryData = SafeMutableLiveData("")
    lateinit var displayedText: String
    private var searchMatches: List<String> = arrayListOf()

    private var searchJob: Job? = null

    fun goBack() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.GO_BACK))
    }

    fun openChatBot() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.OPEN_CHATBOT))
    }

    fun searchQuery(query: String?) {
        searchJob?.cancel()

        this.queryData.value = query?.trim() ?: ""

        if (queryData.value.length >= 2) {
            searchJob = viewModelScope.launch {
                try {

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

                } catch (cancelException: CancellationException) {
                    L.d("Job cancelled")
                }

            }
        } else {
            resetSearch()
        }

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

    fun findPositionOfNextResult( overrideQueryDataLength: Int? = null) {
        if (searchResultCount.value <= 0) {
            return
        }

        var index = findIndexOfNextResult(
            lastMarkedIndex.value + (overrideQueryDataLength ?: queryData.value.length)
        )
        if (index == -1) {
            // the search match was not found, let's search from the beginning
            index = findIndexOfNextResult( 0)
        }
        lastMarkedIndex.value = index
    }

    private fun findIndexOfNextResult(startIndex: Int): Int {
        return displayedText.indexOfAny(searchMatches, startIndex, ignoreCase = true)
    }

}
