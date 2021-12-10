package fi.riista.mobile.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.BindingAdapter
import fi.riista.mobile.R

object BindingAdapters {

    @BindingAdapter(value = ["onClickOpenUrlInBrowser", "onClickOpenUrlInBrowserConfirmationMessage"], requireAll = true)
    @JvmStatic
    fun bindShowConfirmDialogOnOpeningUrlClickListener(view: View, url: String?, confirmationMessage: String?) {
        if (url != null) {
            view.setOnClickListener { v ->
                val context = v.context

                AlertDialog.Builder(context, R.style.AlertDialog_Default)
                        .setMessage(confirmationMessage)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                            openUrlInBrowser(context, url)
                        }
                        .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> /* Do nothing */ }
                        .show()
            }
        }
    }

    private fun openUrlInBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No Web browser found", Toast.LENGTH_SHORT).show()
        }
    }
}
