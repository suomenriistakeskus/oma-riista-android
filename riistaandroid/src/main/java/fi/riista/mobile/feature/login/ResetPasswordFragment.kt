package fi.riista.mobile.feature.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import fi.riista.common.RiistaSDK
import fi.riista.common.model.Language
import fi.riista.mobile.R
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.utils.AppPreferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ResetPasswordFragment : Fragment() {

    interface Manager {
        fun returnToLogin()
    }

    private lateinit var manager: Manager
    private lateinit var emailInput: TextInputEditText
    private lateinit var resetPasswordLayout: ViewGroup
    private lateinit var passwordResetRequestedLayout: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)
        resetPasswordLayout = view.findViewById(R.id.reset_password_layout)
        passwordResetRequestedLayout = view.findViewById(R.id.password_reset_requested_layout)

        val restoreEmailButton = view.findViewById<AppCompatButton>(R.id.btn_restore_email).also { button ->
            button.isEnabled = true
            button.setOnClickListener {
                restoreEmail(button)
            }
        }
        emailInput = view.findViewById<TextInputEditText?>(R.id.restore_email_address).also {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // nop
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // nop
                }

                override fun afterTextChanged(editable: Editable?) {
                    val email = editable?.toString() ?: ""
                    restoreEmailButton.isEnabled = email.trim().length >= 3
                }
            })

            val currentEmail = it.text?.toString() ?: ""
            restoreEmailButton.isEnabled = currentEmail.trim().length >= 3
        }
        view.findViewById<AppCompatButton>(R.id.btn_return_to_login1).also { button ->
            button.setOnClickListener {
                manager.returnToLogin()
            }
        }
        view.findViewById<AppCompatButton>(R.id.btn_return_to_login2).also { button ->
            button.setOnClickListener {
                manager.returnToLogin()
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        manager = context as Manager
    }

    private fun restoreEmail(button: AppCompatButton) {
        button.isEnabled = false
        val email = emailInput.text?.toString() ?: ""
        MainScope().launch {
            val lang = Language.fromLanguageCode(AppPreferences.getLanguageCodeSetting(context)) ?: Language.FI
            val response = RiistaSDK.sendPasswordForgottenEmail(email, lang)
            if (!isResumed) {
                return@launch
            }
            button.isEnabled = true
            response.onError { _, _ ->
                AlertDialogFragment.Builder(requireContext(), AlertDialogId.RESET_PASSWORD_FRAGMENT_OPERATION_FAILED)
                    .setMessage(R.string.group_hunting_operation_failed)
                    .setPositiveButton(R.string.ok)
                    .build()
                    .show(requireActivity().supportFragmentManager)
            }
            response.onSuccessWithoutData {
                resetPasswordLayout.visibility = View.GONE
                passwordResetRequestedLayout.visibility = View.VISIBLE
            }
        }
    }
}
