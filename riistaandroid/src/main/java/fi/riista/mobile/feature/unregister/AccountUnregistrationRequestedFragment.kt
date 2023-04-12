package fi.riista.mobile.feature.unregister

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import fi.riista.common.model.LocalDateTime
import fi.riista.mobile.R
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.riistaSdkHelpers.fromJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalDate
import fi.riista.mobile.riistaSdkHelpers.toJodaLocalTime
import fi.riista.mobile.utils.DateTimeUtils
import fi.riista.mobile.utils.toVisibility
import org.joda.time.DateTime

class AccountUnregistrationRequestedFragment : PageFragment() {

    interface Listener {
        fun onContinueServiceClicked()
        fun onCancelClicked()
    }

    lateinit var listener: Listener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = requireNotNull(context as? Listener) {
            "Context is required to implement Listener interface"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unregistration_common_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewTitle(R.string.unregister_account_request_sent_title_short)

        view.findViewById<TextView>(R.id.tv_header).apply {
            text = getString(R.string.unregister_account_request_sent_title)
        }

        view.findViewById<TextView>(R.id.tv_message).apply {
            val formattedUnregistrationRequestedTime = getUnregistrationRequestedTime(requireArguments())
                .let {
                    val formattedDate = DateTimeUtils.formatLocalDateUsingLongFinnishFormat(it.date.toJodaLocalDate())
                    val formattedTime = DateTimeUtils.formatTime(it.time.toJodaLocalTime())

                    "$formattedDate\u00A0$formattedTime"
                }

            text = getString(
                R.string.unregister_account_request_sent_message_at_timeime,
                formattedUnregistrationRequestedTime
            )
        }

        view.findViewById<MaterialButton>(R.id.btn_positive).apply {
            visibility = getEnableContinueUsingService(requireArguments()).toVisibility()
            text = getString(R.string.unregister_account_continue_using)
            setOnClickListener {
                listener.onContinueServiceClicked()
            }
        }

        view.findViewById<MaterialButton>(R.id.btn_negative).apply {
            visibility = View.GONE
        }

        view.findViewById<MaterialButton>(R.id.btn_cancel).apply {
            visibility = getEnableCancel(requireArguments()).toVisibility()
            setOnClickListener {
                listener.onCancelClicked()
            }
        }
    }

    companion object {
        fun create(
            unregistrationRequestedDatetime: LocalDateTime,
            enableContinueUsingService: Boolean,
            enableCancel: Boolean,
        ): AccountUnregistrationRequestedFragment {
            return AccountUnregistrationRequestedFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_UNREGISTRATION_REQUESTED_DATE_TIME, unregistrationRequestedDatetime.toJodaDateTime())
                    putBoolean(KEY_ENABLE_CONTINUE_USING_SERVICE, enableContinueUsingService)
                    putBoolean(KEY_ENABLE_CANCEL, enableCancel)
                }
            }
        }

        private fun getUnregistrationRequestedTime(arguments: Bundle): LocalDateTime {
            val storedDateTime = arguments.getSerializable(KEY_UNREGISTRATION_REQUESTED_DATE_TIME) as? DateTime
            return requireNotNull(
                value = storedDateTime?.let { LocalDateTime.fromJodaDateTime(it) }
            ) {
                "unregistration request datetime is required"
            }
        }

        private fun getEnableContinueUsingService(arguments: Bundle): Boolean =
            arguments.getBoolean(KEY_ENABLE_CONTINUE_USING_SERVICE, false)

        private fun getEnableCancel(arguments: Bundle): Boolean =
            arguments.getBoolean(KEY_ENABLE_CANCEL, false)

        const val TAG = "AccountUnregistrationRequestedFragment"
        private const val KEY_UNREGISTRATION_REQUESTED_DATE_TIME = "${TAG}_KEY_UNREGISTRATION_REQUESTED_DATE_TIME"
        private const val KEY_ENABLE_CONTINUE_USING_SERVICE = "${TAG}_KEY_ENABLE_CONTINUE_USING_SERVICE"
        private const val KEY_ENABLE_CANCEL = "${TAG}_KEY_ENABLE_CANCEL"
    }
}