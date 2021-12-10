package fi.riista.mobile.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import fi.riista.common.messages.Message
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider


class MessageDialogFragment : DialogFragment() {

    private lateinit var titleTextView: TextView
    private lateinit var messageTextView: TextView

    private lateinit var languageProvider: LanguageProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message_dialog, container, false)

        languageProvider = AppLanguageProvider(context = requireContext())

        titleTextView = view.findViewById(R.id.tv_title)
        messageTextView = view.findViewById<TextView>(R.id.tv_message).apply {
            // let message be scrollable if it doesn't fit the screen
            movementMethod = ScrollingMovementMethod()
        }
        view.findViewById<MaterialButton>(R.id.btn_close).setOnClickListener {
            dismiss()
        }

        val message = getMessageFromArgs(requireArguments())
        val language = languageProvider.getCurrentLanguage()

        titleTextView.text = message.localizedTitle(language)
        messageTextView.text = message.localizedMessage(language)

        return view
    }

    companion object {
        private const val ARGS_MESSAGE = "MDF_message"

        fun create(message: Message): MessageDialogFragment {
            return MessageDialogFragment().apply {
                arguments = Bundle().also { bundle ->
                    bundle.putString(ARGS_MESSAGE, message.serializeToJson())
                }
            }
        }

        private fun getMessageFromArgs(args: Bundle): Message {
            return requireNotNull(args.getString(ARGS_MESSAGE)?.deserializeFromJson()) {
                "Message is required"
            }
        }
    }
}
