package fi.riista.mobile.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * Fragment for showing dialogs. Use [AlertDialogFragment.Builder] to create and show a dialog.
 */
class AlertDialogFragment : DialogFragment() {

    interface Listener {
        fun positiveClicked(dialogId: AlertDialogId, value: String?)
        fun negativeClicked(dialogId: AlertDialogId, value: String?)
        fun itemClicked(dialogId: AlertDialogId, which: Int)
    }

    enum class SelectionType {
        POSITIVE,
        NEGATIVE,
        ITEM,
        ;
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getTitleFromArgs(args))
        builder.setMessage(getMessageFromArgs(args))
        getPositiveTextFromArgs(args)?.let { text ->
            builder.setPositiveButton(text) { _, _ ->
                val requestCode = getDialogId(args)
                val positiveValue = getPositiveValueFromArgs(args)
                val bundle = Bundle().also {
                    it.putString(KEY_DIALOG_RESULT_TYPE, SelectionType.POSITIVE.name)
                    it.putString(KEY_DIALOG_RESULT_TEXT, positiveValue)
                }
                requireActivity().supportFragmentManager.setFragmentResult(requestCode.name, bundle)
            }
        }
        getNegativeTextFromArgs(args)?.let { text ->
            builder.setNegativeButton(text) { _, _ ->
                val requestCode = getDialogId(args)
                val negativeValue = getNegativeValueFromArgs(args)
                val bundle = Bundle().also {
                    it.putString(KEY_DIALOG_RESULT_TYPE, SelectionType.NEGATIVE.name)
                    it.putString(KEY_DIALOG_RESULT_TEXT, negativeValue)
                }
                requireActivity().supportFragmentManager.setFragmentResult(requestCode.name, bundle)
            }
        }
        getIconIdFromArgs(args)?.let { iconId ->
            builder.setIcon(iconId)
        }
        getItemsFromArgs(args)?.let { items ->
            builder.setItems(items) { _, which ->
                val requestCode = getDialogId(args)
                val bundle = Bundle().also {
                    it.putString(KEY_DIALOG_RESULT_TYPE, SelectionType.ITEM.name)
                    it.putInt(KEY_DIALOG_RESULT_ITEM_NUMBER, which)
                }
                requireActivity().supportFragmentManager.setFragmentResult(requestCode.name, bundle)
            }
        }
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.let {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog?.window?.attributes = params
        }
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }

    companion object {
        const val KEY_DIALOG_RESULT_TYPE = "AlertDialogFragmentResultType"
        const val KEY_DIALOG_RESULT_TEXT = "AlertDialogFragmentResultText"
        const val KEY_DIALOG_RESULT_ITEM_NUMBER = "AlertDialogFragmentItemNumber"
        private const val TAG = "AlertDialogFragment"
        private const val ARGS_DIALOG_ID = "ADF_dialog_id"
        private const val ARGS_TITLE = "ADF_title"
        private const val ARGS_MESSAGE = "ADF_message"
        private const val ARGS_POSITIVE_TEXT = "ADF_positive_text"
        private const val ARGS_POSITIVE_VALUE = "ADF_positive_value"
        private const val ARGS_NEGATIVE_TEXT = "ADF_negative_text"
        private const val ARGS_NEGATIVE_VALUE = "ADF_negative_value"
        private const val ARGS_ICON_ID = "ADF_icon_id"
        private const val ARGS_ITEMS = "ADF_items"

        private fun getDialogId(args: Bundle): AlertDialogId {
            return AlertDialogId.fromInt(args.getInt(ARGS_DIALOG_ID))
        }

        private fun getTitleFromArgs(args: Bundle): String? {
            return args.getString(ARGS_TITLE)
        }

        private fun getMessageFromArgs(args: Bundle): String? {
            return args.getString(ARGS_MESSAGE)
        }

        private fun getPositiveTextFromArgs(args: Bundle): String? {
            return args.getString(ARGS_POSITIVE_TEXT)
        }

        private fun getPositiveValueFromArgs(args: Bundle): String? {
            return args.getString(ARGS_POSITIVE_VALUE)
        }

        private fun getNegativeTextFromArgs(args: Bundle): String? {
            return args.getString(ARGS_NEGATIVE_TEXT)
        }

        private fun getNegativeValueFromArgs(args: Bundle): String? {
            return args.getString(ARGS_NEGATIVE_VALUE)
        }

        private fun getIconIdFromArgs(args: Bundle): Int? {
            if (args.containsKey(ARGS_ICON_ID)) {
                return args.getInt(ARGS_ICON_ID)
            }
            return null
        }

        private fun getItemsFromArgs(args: Bundle): Array<CharSequence>? {
            return args.getCharSequenceArray(ARGS_ITEMS)
        }
    }

    class Builder(val context: Context, val alertDialogId: AlertDialogId) {
        private var title: String? = null
        private var message: String? = null
        private var positiveText: String? = null
        private var positiveValue: String? = null
        private var negativeText: String? = null
        private var negativeValue: String? = null
        private var iconId: Int? = null
        private var items: Array<CharSequence>? = null

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setTitle(@StringRes resId: Int): Builder {
            return setTitle(context.getString(resId))
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes resId: Int): Builder {
            return setMessage(context.getString(resId))
        }

        fun setPositiveButton(text: String, value: String? = null): Builder {
            this.positiveText = text
            this.positiveValue = value
            return this
        }

        fun setPositiveButton(@StringRes resId: Int, value: String? = null): Builder {
            return setPositiveButton(context.getString(resId), value)
        }

        fun setNegativeButton(text: String, value: String? = null): Builder {
            this.negativeText = text
            this.negativeValue = value
            return this
        }

        fun setNegativeButton(@StringRes resId: Int, value: String? = null): Builder {
            return setNegativeButton(context.getString(resId), value)
        }

        fun setIcon(@DrawableRes iconId: Int): Builder {
            this.iconId = iconId
            return this
        }

        fun setItems(items: Array<CharSequence>): Builder {
            this.items = items
            return this
        }

        fun build(): AlertDialogFragment {
            val dialog = AlertDialogFragment()
            return setupBundle(dialog)
        }

        private fun setupBundle(dialog: AlertDialogFragment): AlertDialogFragment {
            return dialog.apply {
                arguments = Bundle().also { bundle ->
                    bundle.putInt(ARGS_DIALOG_ID, alertDialogId.id)
                    bundle.putString(ARGS_TITLE, title)
                    bundle.putString(ARGS_MESSAGE, message)
                    bundle.putString(ARGS_POSITIVE_TEXT, positiveText)
                    bundle.putString(ARGS_POSITIVE_VALUE, positiveValue)
                    bundle.putString(ARGS_NEGATIVE_TEXT, negativeText)
                    bundle.putString(ARGS_NEGATIVE_VALUE, negativeValue)
                    bundle.putCharSequenceArray(ARGS_ITEMS, items)
                    iconId?.let { icon -> bundle.putInt(ARGS_ICON_ID, icon) }
                }
            }
        }
    }
}

fun <T> T.registerAlertDialogFragmentResultListener(
    requestCode: AlertDialogId,
    activity: FragmentActivity,
) where T : AlertDialogFragment.Listener  {
    activity.supportFragmentManager.setFragmentResultListener(
        requestCode.name,
        activity,
    ) { code, result ->
        val selectionType = result.getString(AlertDialogFragment.KEY_DIALOG_RESULT_TYPE)
        val text = result.getString(AlertDialogFragment.KEY_DIALOG_RESULT_TEXT)
        val itemNumber = result.getInt(AlertDialogFragment.KEY_DIALOG_RESULT_ITEM_NUMBER)
        val dialogId = AlertDialogId.valueOf(code)
        when (selectionType) {
            AlertDialogFragment.SelectionType.POSITIVE.name -> positiveClicked(dialogId, text)
            AlertDialogFragment.SelectionType.NEGATIVE.name -> negativeClicked(dialogId, text)
            AlertDialogFragment.SelectionType.ITEM.name -> itemClicked(dialogId, itemNumber)
        }
    }
}

fun FragmentActivity.registerAlertDialogFragmentResultListener(
    dialogId: AlertDialogId,
    onPositive: ((String?) -> Unit)? = null,
    onNegative: ((String?) -> Unit)? = null,
    onItem: ((Int) -> Unit)? = null,
) {
    supportFragmentManager.setFragmentResultListener(
        dialogId.requestCode,
        this,
    ) { _, result ->
        val selectionType = result.getString(AlertDialogFragment.KEY_DIALOG_RESULT_TYPE)
        val text = result.getString(AlertDialogFragment.KEY_DIALOG_RESULT_TEXT)
        val itemNumber = result.getInt(AlertDialogFragment.KEY_DIALOG_RESULT_ITEM_NUMBER)
        when (selectionType) {
            AlertDialogFragment.SelectionType.POSITIVE.name -> onPositive?.invoke(text)
            AlertDialogFragment.SelectionType.NEGATIVE.name -> onNegative?.invoke(text)
            AlertDialogFragment.SelectionType.ITEM.name -> onItem?.invoke(itemNumber)
        }
    }
}

private val AlertDialogId.requestCode: String
    get() = name
