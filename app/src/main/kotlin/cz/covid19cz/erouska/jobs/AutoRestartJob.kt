package cz.covid19cz.erouska.jobs

import android.app.AlarmManager
import android.app.AlarmManager.INTERVAL_DAY
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.receiver.AutoRestartReceiver
import cz.covid19cz.erouska.utils.L
import org.threeten.bp.ZonedDateTime

class AutoRestartJob {

    private var pendingIntent: PendingIntent? = null
    private var alarmManager: AlarmManager? = null

    fun setUp(context: Context, alarmManager: AlarmManager) {
        val uri =
            Uri.parse("${context.getString(R.string.uri_scheme)}://${AutoRestartReceiver.URI_PATH}")

        val intent = Intent(context, AutoRestartReceiver::class.java)
            .setAction(Intent.ACTION_RUN)
            .setData(uri)

        pendingIntent = PendingIntent.getBroadcast(
            context,
            AutoRestartReceiver.REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        this.alarmManager = alarmManager

        val first = ZonedDateTime.now().plusDays(1).withHour(2).withMinute(0)

        L.d("Planning auto-restart with interval 24h, first: $first")

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            first.toInstant().toEpochMilli(),
            INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancel() {
        L.d("Cancelling auto-restart")
        pendingIntent?.let { alarmManager?.cancel(it) }
    }
}