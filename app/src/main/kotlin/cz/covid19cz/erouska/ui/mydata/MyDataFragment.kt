package cz.covid19cz.erouska.ui.mydata

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ErouskaWidget
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentMyDataBinding
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ext.hide
import cz.covid19cz.erouska.ext.show
import cz.covid19cz.erouska.ext.showWeb
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.dashboard.DashboardFragment
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.dashboard.event.GmsApiErrorEvent
import cz.covid19cz.erouska.ui.exposure.event.ExposuresCommandEvent
import cz.covid19cz.erouska.ui.mydata.event.MyDataCommandEvent
import cz.covid19cz.erouska.updateAppWidget
import cz.covid19cz.erouska.utils.Analytics
import cz.covid19cz.erouska.utils.Analytics.KEY_CURRENT_MEASURES
import cz.covid19cz.erouska.utils.CustomTabHelper
import cz.covid19cz.erouska.utils.L
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_data.*
import javax.inject.Inject

@AndroidEntryPoint
class MyDataFragment :
    BaseFragment<FragmentMyDataBinding, MyDataVM>(R.layout.fragment_my_data, MyDataVM::class) {

    @Inject
    internal lateinit var customTabHelper: CustomTabHelper
    @Inject
    lateinit var prefs: SharedPrefsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AppConfig.updateNewsOnRequest) {
            refresh_container.isEnabled = false
        }

        measures_text.setOnClickListener {
            openMeasures()
            Analytics.logEvent(requireContext(), KEY_CURRENT_MEASURES)
        }
    }

    private fun subscribeToViewModel() {
        subscribe(MyDataCommandEvent::class) { commandEvent ->
            when (commandEvent.command) {
                MyDataCommandEvent.Command.UPDATE_WIDGET -> {
                    updateWidget()
                }
            }
        }
    }

    private fun openMeasures() {
        showWeb(viewModel.getMeasuresUrl(), customTabHelper)
    }

    private fun updateWidget() {
        L.i("Update widget")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val man = AppWidgetManager.getInstance(context)
        val ids = man.getAppWidgetIds(ComponentName(requireContext(), ErouskaWidget::class.java))

        for (appWidgetId in ids) {
            updateAppWidget(requireContext(), appWidgetManager, appWidgetId, prefs)
        }
    }
}
