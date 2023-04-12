package fi.riista.mobile.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import fi.riista.mobile.R

fun Boolean.toVisibility() =
    if (this) {
        View.VISIBLE
    } else {
        View.GONE
    }

fun Uri.openInBrowser(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, this)

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, context.getText(R.string.no_web_browser), Toast.LENGTH_SHORT).show()
    }
}

/**
 * Attempts to open the uri in Google Play
 **/
fun Uri.openInGooglePlay(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, this)

    // only allow opening google play. It is possible that third-party apps register their
    // own intent filters for "market://" scheme. Don't allow those.
    intent.setPackage("com.google.vending")

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Uri.parse("https://play.google.com/store/apps/details?$query").openInBrowser(context)
    }
}