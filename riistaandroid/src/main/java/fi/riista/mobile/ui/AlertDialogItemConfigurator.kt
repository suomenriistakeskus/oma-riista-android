package fi.riista.mobile.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

fun AlertDialog.Builder.configureItems(
    configure: AlertDialogItemConfigurator.() -> Unit
): AlertDialog.Builder {
    val configurator = AlertDialogItemConfigurator(context = context)
    configurator.configure()

    setItems(configurator.titles) { _, selectedIndex ->
        configurator.handleClicked(index = selectedIndex)
    }

    return this
}

class AlertDialogItemConfigurator(
    private val context: Context
) {
    private var items = mutableListOf<AlertDialogItem>()

    val titles: Array<String>
        get() = items.map { it.title }.toTypedArray()

    @Suppress("MemberVisibilityCanBePrivate")
    fun addItem(title: String, onClicked: () -> Unit) {
        items.add(AlertDialogItem(title, onClicked))
    }

    fun addItem(@StringRes titleRes: Int, onClicked: () -> Unit) {
        addItem(title = context.getString(titleRes), onClicked)
    }

    fun handleClicked(index: Int) {
        items.getOrNull(index)?.onClicked?.invoke()
    }

}

private data class AlertDialogItem(
    val title: String,
    val onClicked: () -> Unit,
)