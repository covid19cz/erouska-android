package cz.covid19cz.erouska

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import androidx.navigation.NavDeepLinkBuilder
import arch.livedata.SafeMutableLiveData
import com.google.firebase.auth.FirebaseAuth
import cz.covid19cz.erouska.db.SharedPrefsRepository
import cz.covid19cz.erouska.ui.dashboard.event.DashboardCommandEvent
import cz.covid19cz.erouska.ui.mydata.MyDataVM
import cz.covid19cz.erouska.utils.SignNumberFormat
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Implementation of App Widget functionality.
 */
@AndroidEntryPoint
class ErouskaWidget : AppWidgetProvider() {
    @Inject
    lateinit var prefs: SharedPrefsRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, prefs)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    prefs: SharedPrefsRepository
) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Data Declaration **/
    val testsTotal = prefs.getTestsTotal()
    val testsIncrease = prefs.getTestsIncrease()
    val testsIncreaseDate= if (prefs.getTestsIncreaseDate() == 0L) {
        "-"
    } else {
        SimpleDateFormat(
            MyDataVM.LAST_UPDATE_UI_FORMAT,
            Locale.getDefault()
        ).format(Date(prefs.getTestsIncreaseDate()))
    }

    val confirmedCasesTotal = prefs.getConfirmedCasesTotal()
    val confirmedCasesIncrease = prefs.getConfirmedCasesIncrease()
    val confirmedCasesIncreaseDate= if (prefs.getConfirmedCasesIncreaseDate() == 0L) {
        "-"
    } else {
        SimpleDateFormat(
            MyDataVM.LAST_UPDATE_UI_FORMAT,
            Locale.getDefault()
        ).format(Date(prefs.getConfirmedCasesIncreaseDate()))
    }

    val activeCasesTotal = prefs.getActiveCasesTotal()
    val curedCasesTotal = prefs.getCuredTotal()


    /** View Declaration **/
    val views = RemoteViews(context.packageName, R.layout.erouska_widget)


    val itemTests = RemoteViews(context.packageName, R.layout.widget_data_item)
    itemTests.setTextViewText(R.id.title_text, context.getString(R.string.my_data_tests, NumberFormat.getInstance().format(testsTotal)))
    itemTests.setTextViewText(R.id.subtitle_text, context.getString(R.string.my_data_for_day, SignNumberFormat.format(testsIncrease), testsIncreaseDate))
    itemTests.setImageViewResource(R.id.icon, R.drawable.ic_test)

    val itemAckCases = RemoteViews(context.packageName, R.layout.widget_data_item)
    itemAckCases.setTextViewText(R.id.title_text, context.getString(R.string.my_data_ack_cases, NumberFormat.getInstance().format(confirmedCasesTotal)))
    itemAckCases.setTextViewText(R.id.subtitle_text, context.getString(R.string.my_data_for_day, SignNumberFormat.format(confirmedCasesIncrease), confirmedCasesIncreaseDate))
    itemAckCases.setImageViewResource(R.id.icon, R.drawable.ic_ack_case)

    val itemActCases = RemoteViews(context.packageName, R.layout.widget_data_item_plain)
    itemActCases.setTextViewText(R.id.title_text, context.getString(R.string.my_data_act_cases, NumberFormat.getInstance().format(activeCasesTotal)))
    itemActCases.setImageViewResource(R.id.icon, R.drawable.ic_act_case)

    val itemCured = RemoteViews(context.packageName, R.layout.widget_data_item_plain)
    itemCured.setTextViewText(R.id.title_text, context.getString(R.string.my_data_cured, NumberFormat.getInstance().format(curedCasesTotal)))
    itemCured.setImageViewResource(R.id.icon, R.drawable.ic_cured)

    views.removeAllViews(R.id.view_container)
    views.addView(R.id.view_container, itemTests)
    views.addView(R.id.view_container, itemAckCases)
    views.addView(R.id.view_container, itemActCases)
//    views.addView(R.id.view_container, itemCured)

    val pendingIntent = if (auth.currentUser == null) {
        NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.nav_welcome_fragment)
            .createPendingIntent()
    } else {
        NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.nav_my_data)
            .createPendingIntent()
    }
    views.setOnClickPendingIntent(R.id.view_container, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}