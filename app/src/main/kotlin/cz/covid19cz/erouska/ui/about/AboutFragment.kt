package cz.covid19cz.erouska.ui.about

import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentAboutBinding
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.base.UrlEvent
import cz.covid19cz.erouska.utils.CustomTabHelper
import org.koin.android.ext.android.inject

class AboutFragment : BaseFragment<FragmentAboutBinding, AboutVM>(R.layout.fragment_about, AboutVM::class) {

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
    }
}
