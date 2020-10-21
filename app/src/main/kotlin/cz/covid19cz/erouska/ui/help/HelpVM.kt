package cz.covid19cz.erouska.ui.help

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import arch.livedata.SafeMutableLiveData
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent
import cz.covid19cz.erouska.utils.L
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class HelpVM @ViewModelInject constructor() : BaseVM() {

    val searchControlsEnabled = SafeMutableLiveData(false)
    val searchResultCount = SafeMutableLiveData(0)
    val queryData = SafeMutableLiveData("")
    val content = SafeMutableLiveData(AppConfig.helpMarkdown)
    val lastMarkedIndex = SafeMutableLiveData(0)

    private var searchJob: Job? = null

    fun goBack() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.GO_BACK))
    }

    fun openChatBot() {
        publish(HelpCommandEvent(HelpCommandEvent.Command.OPEN_CHATBOT))
    }

    fun searchQuery(query: String?, faqContent: String) {
        searchJob?.cancel()

        this.queryData.value = query?.trim() ?: ""

        if (queryData.value.length >= 3) {
            searchJob = viewModelScope.launch {
                try {

                    val pattern = queryData.value
                    val r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                    var result = AppConfig.helpMarkdown
                    val m = r.matcher(result)
                    val replaceList = arrayListOf<String>()

                    while (m.find()) {
                        replaceList.add(result.substring(m.start(0), m.end(0)))
                    }
                    searchResultCount.value = replaceList.size

                    for (replaceString in replaceList.distinct()) {
                        result = result.replace(replaceString, "[[${replaceString}]]")
                    }

                    content.value = result
                    searchControlsEnabled.value = replaceList.isNotEmpty()
                    lastMarkedIndex.value = faqContent.indexOf(queryData.value, ignoreCase = true)

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


    fun findPositionOfPreviousResult(query: String, content: String) {
        if (searchResultCount.value > 0) {
            if (lastMarkedIndex.value == -1) {
                lastMarkedIndex.value = content.length
            }
            lastMarkedIndex.value = content.substring(0, lastMarkedIndex.value).lastIndexOf(
                query, ignoreCase = true
            )
        }
    }

    fun findPositionOfNextResult(query: String, content: String) {
        if (searchResultCount.value > 0) {
            lastMarkedIndex.value = content.indexOf(
                query,
                lastMarkedIndex.value + query.length,
                ignoreCase = true
            )
        }
    }

}
