package fi.riista.mobile.feature.unregister

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import fi.riista.mobile.R
import fi.riista.mobile.pages.PageFragment
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.registerAlertDialogFragmentResultListener

class AskAccountUnregistrationFragment : PageFragment() {

    interface Listener {
        fun onRequestAccountUnregistration()
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

        setViewTitle(R.string.unregister_account_title)

        view.findViewById<TextView>(R.id.tv_header).apply {
            visibility = View.GONE
        }

        view.findViewById<TextView>(R.id.tv_message).apply {
            text = getString(R.string.unregister_account_message)
        }

        view.findViewById<MaterialButton>(R.id.btn_positive).apply {
            visibility = View.GONE
        }

        view.findViewById<MaterialButton>(R.id.btn_negative).apply {
            text = getString(R.string.unregister_account_title)
            setOnClickListener {
                askAccountUnregistrationConfirmation()
            }
        }

        view.findViewById<MaterialButton>(R.id.btn_cancel).apply {
            visibility = View.GONE
        }

        requireActivity().registerAlertDialogFragmentResultListener(
            dialogId = AlertDialogId.UNREGISTER_CONFIRM_UNREGISTRATION,
            onPositive = {
                listener.onRequestAccountUnregistration()
            }
        )
    }

    private fun askAccountUnregistrationConfirmation() {
        AlertDialogFragment.Builder(requireActivity(), AlertDialogId.UNREGISTER_CONFIRM_UNREGISTRATION)
            .setMessage(R.string.unregister_account_question)
            .setPositiveButton(R.string.yes)
            .setNegativeButton(R.string.no)
            .build()
            .show(requireActivity().supportFragmentManager)
    }

    companion object {
        const val TAG = "AskAccountUnregistrationFragment"
    }
}