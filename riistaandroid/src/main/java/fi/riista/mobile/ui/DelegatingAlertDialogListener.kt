package fi.riista.mobile.ui

import androidx.fragment.app.FragmentActivity

class DelegatingAlertDialogListener(val activity: FragmentActivity) : AlertDialogFragment.Listener {

    private val dialogIds = mutableListOf<AlertDialogId>()
    private var positiveCallbacks = mutableMapOf<AlertDialogId, (String?) -> Unit>()
    private var negativeCallbacks = mutableMapOf<AlertDialogId, (String?) -> Unit>()
    private var itemCallbacks = mutableMapOf<AlertDialogId, (Int) -> Unit>()

    fun registerPositiveCallback(dialogId: AlertDialogId, callback: (String?) -> Unit) {
        registerForAlertDialogId(dialogId)
        positiveCallbacks[dialogId] = callback
    }

    fun registerNegativeCallback(dialogId: AlertDialogId, callback: (String?) -> Unit) {
        registerForAlertDialogId(dialogId)
        negativeCallbacks[dialogId] = callback
    }

    fun registerItemCallback(dialogId: AlertDialogId, callback: (Int) -> Unit) {
        registerForAlertDialogId(dialogId)
        itemCallbacks[dialogId] = callback
    }

    override fun positiveClicked(dialogId: AlertDialogId, value: String?) {
        registerForAlertDialogId(dialogId)
        positiveCallbacks[dialogId]?.let { callback ->
            callback(value)
        }
    }

    override fun negativeClicked(dialogId: AlertDialogId, value: String?) {
        registerForAlertDialogId(dialogId)
        negativeCallbacks[dialogId]?.let { callback ->
            callback(value)
        }
    }

    override fun itemClicked(dialogId: AlertDialogId, which: Int) {
        registerForAlertDialogId(dialogId)
        itemCallbacks[dialogId]?.let { callback ->
            callback(which)
        }
    }

    private fun registerForAlertDialogId(dialogId: AlertDialogId) {
        if (!dialogIds.contains(dialogId)) {
            this.registerAlertDialogFragmentResultListener(dialogId, activity)
            dialogIds.add(dialogId)
        }
    }
}
