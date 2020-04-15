package cz.covid19cz.erouska.ui.help

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentHelpBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.help.HelpFragment.InfoType.DataCollectionType
import cz.covid19cz.erouska.ui.help.HelpFragment.InfoType.HelpType
import cz.covid19cz.erouska.ui.help.event.HelpCommandEvent
import cz.covid19cz.erouska.utils.Markdown
import kotlinx.android.synthetic.main.fragment_help.*
import org.koin.android.ext.android.inject

class HelpFragment : BaseFragment<FragmentHelpBinding, HelpVM>(R.layout.fragment_help, HelpVM::class) {

    private val markdown by inject<Markdown>()
    private var isFullscreen: Boolean = false
    private lateinit var type: InfoType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribe(HelpCommandEvent::class) {
            when (it.command) {
                HelpCommandEvent.Command.GO_BACK -> goBack()
            }
        }

        isFullscreen = arguments?.getBoolean("fullscreen") == true
        type = when (arguments?.getString("type")) {
            "help" -> HelpType()
            "data_collection" -> DataCollectionType()
            else -> HelpType()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (type is HelpType) {
            inflater.inflate(R.menu.help, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (type) {
            is HelpType -> {
                enableUpInToolbar(isFullscreen, IconType.CLOSE)
                markdown.show(help_desc, AppConfig.helpMarkdown)
            }
            is DataCollectionType -> {
                enableUpInToolbar(true)
                markdown.show(help_desc, AppConfig.dataCollectionMarkdown)
            }
        }

        if (isFullscreen) {
            welcome_continue_btn.visibility = View.VISIBLE
        } else {
            welcome_continue_btn.visibility = View.GONE
        }
    }

    fun goBack() {
        navController().navigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_about -> {
                navigate(R.id.nav_about, Bundle().apply {
                    // replicate the
                    putBoolean("fullscreen", isFullscreen)
                })
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    sealed class InfoType {
        class DataCollectionType : InfoType()
        class HelpType : InfoType()
    }
}