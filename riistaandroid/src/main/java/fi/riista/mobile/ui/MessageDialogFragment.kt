package fi.riista.mobile.ui

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import fi.riista.common.messages.Message
import fi.riista.common.messages.MessageLink
import fi.riista.common.model.Language
import fi.riista.common.resources.LanguageProvider
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import fi.riista.mobile.R
import fi.riista.mobile.riistaSdkHelpers.AppLanguageProvider
import fi.riista.mobile.utils.openInBrowser
import fi.riista.mobile.utils.openInGooglePlay


class MessageDialogFragment : DialogFragment() {

    private lateinit var titleTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var linkButton: MaterialButton

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
        linkButton = view.findViewById(R.id.btn_link)

        val message = getMessageFromArgs(requireArguments())
        val language = languageProvider.getCurrentLanguage()

        titleTextView.text = message.localizedTitle(language)
        messageTextView.text = message.localizedMessage(language)

        setupLinkButton(link = message.link, language = language)

        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        arguments
            ?.let { getMessageFromArgs(it).preventFurtherAppUsage }
            ?.let { preventFurtherAppUsage ->
                if (preventFurtherAppUsage) {
                    // this is most likely an app startup message. We cannot actually guarantee that app is fully
                    // closed here. Hopefully whoever instantiated this dialog is able to take this into account.
                    activity?.finishAndRemoveTask()
                }
            }
    }

    private fun setupLinkButton(link: MessageLink?, language: Language) {
        val linkUrl = link?.localizedUrl(language)?.let { Uri.parse(it) }
        if (linkUrl == null) {
            linkButton.visibility = View.GONE
            return
        }

        linkButton.text = link.localizedName(language)
        linkButton.setOnClickListener {
            if (linkUrl.scheme?.startsWith("market") == true) {
                linkUrl.openInGooglePlay(requireContext())
            } else {
                linkUrl.openInBrowser(requireContext())
            }
        }
    }

    companion object {
        private const val ARGS_MESSAGE = "MDF_message"

        /**
         * Instantiates a new [MessageDialogFragment] in order to display given [Message].
         *
         * Note: Will close the current activity with [Activity::finishAndRemoveTask()] upon dialog
         * dismiss if [message.preventFurtherAppUsage] has been set.
         */
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
