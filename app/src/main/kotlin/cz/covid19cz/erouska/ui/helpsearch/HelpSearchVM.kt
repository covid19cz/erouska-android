package cz.covid19cz.erouska.ui.helpsearch

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import arch.adapter.RecyclerLayoutStrategy
import arch.livedata.SafeMutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import java.lang.reflect.Type
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

        val questions = "[\n" +
                "\t{\n" +
                "\t\t\"title\": \"Obecné\",\n" +
                "\t\t\"subtitle\": \"eRouška, karanténa a postupy hygieny\",\n" +
                "\t\t\"icon\": \"https://erouska.cz/img/symptoms/ic_temperature.png\",\n" +
                "\t\t\"questions\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"question\": \"Jak mám postupovat, když mi aplikace zobrazila upozornění na rizikové setkání s nakaženým uživatelem?\",\n" +
                "\t\t\t\t\"answer\": \"Pokud máte příznaky nákazy (například zvýšenou teplotu, kašel, dušnost, bolest v krku, bolest hlavy, náhlou ztrátu čichu a chuti), telefonicky kontaktujte svého praktického lékaře. V případě, že je váš stav opravdu akutní, volejte rychlou záchrannou službu 155, případě linku integrovaného záchranného systému 112. Uvedené příznaky se mohou objevit 2 až 14 dní po rizikovém setkání.\\n\\nPokud nemáte žádné příznaky, chovejte se prosím zodpovědně:\\n\\n*   Noste roušku či respirátor přes ústa a nos.\\n*   Často a důkladně si myjte ruce vodou a mýdlem či používejte dezinfekci.\\n*   Pravidelně dezinfikujte vlastní předměty (např. mobilní telefon).\\n*   Kašlejte a kýchejte do kapesníku či rukávu.\\n*   Používejte jednorázové kapesníky a poté je vyhoďte.\\n*   Vyhýbejte se zbytečnému shromažďování a dodržujte bezpečný odstup od ostatních (přibližně 2 metry).\\n\\nJinak můžete běžně fungovat. Je možné, že vás bude v blízké době kontaktovat pracovník hygienické služby na základě epidemiologického šetření s konkrétním nakaženým. V takovém případě postupujte podle pokynů hygienické služby.\\n\\nPokud by se v průběhu 14 dní od rizikového setkání váš zdravotní stav zhoršil a objevily se u vás příznaky onemocnění COVID-19, kontaktujte svého praktického lékaře.\\n\\n## Jak mám postupovat, když mám pozitivní výsledek testu na COVID-19 nebo mi přišla SMS eRoušky s ověřovacím kódem?\\nJestliže vám přišla SMS z laboratoře s pozitivním výsledkem testu na COVID-19, měla by vám do několika hodin přijít také SMS eRoušky s ověřovacím kódem pro odeslání dat. SMS eRoušky se odesílají automatizovaně po zapsání výsledku testů do informačního systému. Odcházejí denně vždy od 8:00 do 22:00.\\n\\nJakmile vám přijde SMS eRoušky s ověřovacím kódem, [postupujte prosím podle tohoto návodu](https://erouska.cz/sms). Uvedený postup vám umožní anonymně informovat ostatní uživatele o případném rizikovém setkání. Děkujeme.\\n\\nMáte pozitivní test a nepřišla vám SMS eRoušky? Napište si o nový kód na [info@erouska.cz](mailto:info@erouska.cz?subject=).\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"question\": \"Jak mám postupovat, když mám pozitivní výsledek testu na COVID-19 nebo mi přišla SMS eRoušky s ověřovacím kódem?\",\n" +
                "\t\t\t\t\"answer\": \"Jestliže vám přišla SMS z laboratoře s pozitivním výsledkem testu na COVID-19, měla by vám do několika hodin přijít také SMS eRoušky s ověřovacím kódem pro odeslání dat. SMS eRoušky se odesílají automatizovaně po zapsání výsledku testů do informačního systému. Odcházejí denně vždy od 8:00 do 22:00.\\n\\nJakmile vám přijde SMS eRoušky s ověřovacím kódem, [postupujte prosím podle tohoto návodu](https://erouska.cz/sms). Uvedený postup vám umožní anonymně informovat ostatní uživatele o případném rizikovém setkání. Děkujeme.\\n\\nMáte pozitivní test a nepřišla vám SMS eRoušky? Napište si o nový kód na [info@erouska.cz](mailto:info@erouska.cz?subject=).\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"title\": \"Fungování eRoušky\",\n" +
                "\t\t\"subtitle\": \"sběr a vyhodnocení dat, význam upozornění\",\n" +
                "\t\t\"icon\": \"https://erouska.cz/img/symptoms/ic_temperature.png\",\n" +
                "\t\t\"questions\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"question\": \"Jak eRouška zaznamenává a zpracovává data o setkáních uživatelů?\",\n" +
                "\t\t\t\t\"answer\": \"Chytrý telefon s aplikací eRouška zaznamená přes Bluetooth LE anonymní identifikátory (ID) z jiných zařízení s touto aplikací. Informaci o „setkání“ a jeho délce ukládá do své vnitřní paměti.\\n\\nV případě, že budete mít pozitivní výsledek testu na COVID-19, přijde vám jednorázový SMS kód. Zadáte jej do aplikace ([postup zde](https://erouska.cz/sms)), a tím umožníte odeslání svých anonymních identifikátorů na server, odkud si je stáhnou ostatní eRoušky k vyhodnocení. Algoritmus v aplikaci každého uživatele na základě epidemiologického modelu automatizovaně vyhodnotí sesbíraná data – porovná identifikátory se všemi zaznamenanými setkáními. V případě rizikového kontaktu zobrazí upozornění, že hrozí nákaza kvůli setkání s pozitivně testovanou osobou, a doporučí další postup.\\n\\nPodrobné informace o sběru a zpracování dat naleznete v [Informacích o zpracování osobních údajů v aplikaci eRouška 2.0](https://erouska.cz/podminky-pouzivani).\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"question\": \"Jak eRouška vyhodnocuje rizikové setkání?\",\n" +
                "\t\t\t\t\"answer\": \"Epidemiologové stanovují rizikový kontakt jako setkání, které je ve vzdálenosti bližší než 2 metry po dobu alespoň 15 minut. Aplikace eRouška se to snaží co nejpřesněji změřit dostupnými technologiemi. Vzdálenost mezi uživateli, respektive jejich telefony, se odhaduje na základě síly signálu Bluetooth. Doba setkání se posuzuje podle měřicích oken – telefon v několikaminutových intervalech zjišťuje, zda jsou v okolí jiné telefony s eRouškou.\\n\\nVyšší přesnosti měření lze tedy dosáhnout častějším vysíláním a zaznamenáváním dalších zařízení s eRouškou v okolí. To se bohužel promítne do vyšší spotřeby baterie. Algoritmy měření nastavují Apple a Google ve svém Exposure Notification protokolu, na kterém je eRouška postavená. Vývojáři eRoušky mohou pouze částečně upravovat některé parametry a provádět doplňující filtrování výsledků.\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}\n" +
                "]"

        val founderListType: Type = object : TypeToken<ArrayList<Category>>() {}.type
        val structuredQs2: ArrayList<Category> = Gson().fromJson(questions, founderListType)

        val allQuestions = structuredQs2.map {
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
