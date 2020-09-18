package cz.covid19cz.erouska.ext

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.api.Status
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R

fun Status.resolveUnknownGmsError(context : Context){
    AlertDialog.Builder(context).setTitle(context.getString(R.string.dialog_en_api_not_found_title, statusCode))
        .setMessage(R.string.dialog_en_api_not_found_message)
        .setPositiveButton(R.string.ok) { _, _ ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.supportEmail))
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.dialog_en_api_not_found_title, statusCode))
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }.show()
}