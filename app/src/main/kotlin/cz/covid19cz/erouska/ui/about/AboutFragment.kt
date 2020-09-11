package cz.covid19cz.erouska.ui.about

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentAboutBinding
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.base.UrlEvent
import cz.covid19cz.erouska.utils.CustomTabHelper
import kotlinx.android.synthetic.main.fragment_about.*
import org.koin.android.ext.android.inject

class AboutFragment :
    BaseFragment<FragmentAboutBinding, AboutVM>(R.layout.fragment_about, AboutVM::class) {

    private val customTabHelper by inject<CustomTabHelper>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribe(UrlEvent::class) {
            showWeb(it.url, customTabHelper)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true, IconType.UP)

        about_tos_content.text = HtmlCompat.fromHtml(
            getString(R.string.about_tos_content, AppConfig.conditionsOfUseUrl),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }
}
