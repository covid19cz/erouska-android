package cz.covid19cz.erouska.ui.about

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.HtmlCompat
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.BuildConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentAboutBinding
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.base.UrlEvent
import cz.covid19cz.erouska.utils.CustomTabHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_about.*
import java.lang.RuntimeException
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment :
    BaseFragment<FragmentAboutBinding, AboutVM>(R.layout.fragment_about, AboutVM::class) {

    private var easterEggShown = false

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper

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

        about_version.setOnLongClickListener {
            if (easterEggShown) {
                throw RuntimeException("Crashlytics exception test for version ${BuildConfig.VERSION_NAME}.")
            } else {
                Toast.makeText(context, "Ještě jeden long-press a asi něco vypustíme", Toast.LENGTH_LONG).show()
                easterEggShown = true
                true
            }
        }
    }
}
