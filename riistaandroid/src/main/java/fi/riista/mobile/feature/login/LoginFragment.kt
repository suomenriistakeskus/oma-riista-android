package fi.riista.mobile.feature.login

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import fi.riista.common.RiistaSDK
import fi.riista.common.model.Language
import fi.riista.mobile.R
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.KeyboardUtils
import fi.riista.mobile.utils.UiUtils
import fi.riista.mobile.utils.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LoginFragment : Fragment(), LoginAttemptFailedListener {

    interface Manager {
        fun startLogin(username: String, password: String, listener: LoginAttemptFailedListener)
        fun startResetPassword()
        fun startEmailChanged()
    }

    enum class Tab {
        LOGIN_TAB,
        CREATE_ACCOUNT_TAB,
    }

    private var loginButtonCanBeEnabled = true
    private lateinit var manager: Manager
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var resetPasswordButton: MaterialButton
    private lateinit var newEmailInput: TextInputEditText
    private lateinit var loginTab: AppCompatButton
    private lateinit var createAccountTab: AppCompatButton

    private lateinit var tabContainer: ViewGroup
    private lateinit var loginLayout: ViewGroup
    private lateinit var createAccountLayout: ViewGroup
    private lateinit var createAccountLayout2: ViewGroup
    private lateinit var createAccountRequestedLayout: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        usernameInput = view.findViewById(R.id.username)
        passwordInput = view.findViewById(R.id.password)
        loginButton = view.findViewById<MaterialButton>(R.id.btn_login).also { button ->
            button.isEnabled = loginButtonCanBeEnabled
            button.setOnClickListener {
                attemptLogin()
            }
        }
        resetPasswordButton = view.findViewById<MaterialButton>(R.id.btn_reset_password).also { button ->
            button.setOnClickListener {
                manager.startResetPassword()
            }
        }
        view.findViewById<MaterialButton>(R.id.btn_email_changed).also { button ->
            button.setOnClickListener {
                manager.startEmailChanged()
            }
        }

        val createAccountButton = view.findViewById<MaterialButton>(R.id.btn_create_account).also { button ->
            button.isEnabled = true
            button.setOnClickListener {
                button.isEnabled = false
                val email = newEmailInput.text?.toString() ?: ""
                MainScope().launch {
                    val lang = Language.fromLanguageCode(AppPreferences.getLanguageCodeSetting(context)) ?: Language.FI
                    val response = RiistaSDK.sendStartRegistrationEmail(email, lang)
                    if (!isResumed) {
                        return@launch
                    }
                    button.isEnabled = true
                    response.onError { _, _ ->
                        AlertDialog.Builder(context)
                            .setMessage(R.string.group_hunting_operation_failed)
                            .setPositiveButton(R.string.ok, null)
                            .create()
                            .show()
                    }
                    response.onSuccessWithoutData {
                        createAccountLayout.visibility = View.GONE
                        createAccountRequestedLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
        newEmailInput = view.findViewById<TextInputEditText>(R.id.new_email).also {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // nop
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // nop
                }

                override fun afterTextChanged(editable: Editable?) {
                    val email = editable?.toString() ?: ""
                    createAccountButton.isEnabled = email.trim().length >= 3
                }
            })

            val currentEmail = it.text?.toString() ?: ""
            createAccountButton.isEnabled = currentEmail.trim().length >= 3
        }
        view.findViewById<MaterialButton>(R.id.btn_return_to_login).also { button ->
            button.setOnClickListener {
                activateTab(Tab.LOGIN_TAB)
            }
        }

        tabContainer = view.findViewById(R.id.tab_container)
        loginLayout = view.findViewById(R.id.login_layout)
        createAccountLayout = view.findViewById(R.id.create_account_layout)
        createAccountLayout2 = view.findViewById(R.id.create_account_layout2)
        createAccountRequestedLayout = view.findViewById(R.id.create_account_requested_layout)

        val activeTab = getActiveTab(requireArguments())
        loginTab = view.findViewById(R.id.tab_login)
        createAccountTab = view.findViewById(R.id.tab_create_account)
        activateTab(activeTab)

        loginTab.setOnClickListener {
            KeyboardUtils.hideKeyboard(requireContext(), tabContainer)
            activateTab(Tab.LOGIN_TAB)
        }
        createAccountTab.setOnClickListener {
            KeyboardUtils.hideKeyboard(requireContext(), tabContainer)
            activateTab(Tab.CREATE_ACCOUNT_TAB)
        }
        return view
    }

    private fun activateTab(tab: Tab) {
        when (tab) {
            Tab.LOGIN_TAB -> {
                loginLayout.visibility = View.VISIBLE
                createAccountLayout.visibility = View.GONE
                createAccountRequestedLayout.visibility = View.GONE
                loginTab.isSelected = true
                createAccountTab.isSelected = false
            }
            Tab.CREATE_ACCOUNT_TAB -> {
                val loginHeight = UiUtils.getViewHeightInPixels(loginLayout)
                setHeight(createAccountLayout, loginHeight)
                setHeight(createAccountRequestedLayout, loginHeight)
                createAccountLayout2.minimumHeight = loginHeight

                loginLayout.visibility = View.GONE
                createAccountLayout.visibility = View.VISIBLE
                createAccountRequestedLayout.visibility = View.GONE
                loginTab.isSelected = false
                createAccountTab.isSelected = true
            }
        }
    }

    private fun setHeight(view: ViewGroup, height: Int) {
        val params = view.layoutParams
        params.height = height
        view.layoutParams = params
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        manager = context as Manager
    }

    override fun onResume() {
        loginButtonCanBeEnabled = Utils.isPlayServicesAvailable(requireActivity(), true)
        loginButton.isEnabled = loginButtonCanBeEnabled
        super.onResume()
    }

    private fun attemptLogin() {
        loginButton.isEnabled = false
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()
        manager.startLogin(
            username = username,
            password = password,
            listener = this,
        )
    }

    companion object {
        private const val ARGS_PREFIX = "LF_args"
        private const val ARGS_ACTIVE_TAB = "${ARGS_PREFIX}_active_tab"

        fun create(activeTab: Tab): LoginFragment {
            return LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_ACTIVE_TAB, activeTab.name)
                }
            }
        }

        private fun getActiveTab(args: Bundle): Tab {
            return Tab.valueOf(
                args.getString(ARGS_ACTIVE_TAB) ?: Tab.LOGIN_TAB.name
            )
        }
    }

    override fun loginFailed() {
        loginButton.isEnabled = loginButtonCanBeEnabled
    }
}
