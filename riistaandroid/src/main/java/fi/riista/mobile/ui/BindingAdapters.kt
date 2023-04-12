package fi.riista.mobile.ui

import android.content.DialogInterface
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.BindingAdapter
import fi.riista.mobile.R
import fi.riista.mobile.utils.openInBrowser

object BindingAdapters {

    @BindingAdapter(
        value = ["onClickOpenUrlInBrowser", "onClickOpenUrlInBrowserConfirmationMessage"],
        requireAll = true
    )

    @JvmStatic
    fun bindShowConfirmDialogOnOpeningUrlClickListener(view: View, url: String?, confirmationMessage: String?) {
        if (url != null) {
            view.setOnClickListener { v ->
                val context = v.context
                val uri = Uri.parse(url)

                AlertDialog.Builder(context, R.style.AlertDialog_Default)
                    .setMessage(confirmationMessage)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        uri.openInBrowser(context)
                    }
                    .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> /* Do nothing */ }
                    .show()
            }
        }
    }
}
