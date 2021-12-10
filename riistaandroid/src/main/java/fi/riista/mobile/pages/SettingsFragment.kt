package fi.riista.mobile.pages

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.setPadding
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.button.MaterialButton
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.mobile.AppConfig
import fi.riista.mobile.ExternalUrls
import fi.riista.mobile.R
import fi.riista.mobile.activity.MainActivity
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.sync.SyncConfig
import fi.riista.mobile.utils.AppPreferences.*
import fi.riista.mobile.utils.BuildInfo
import fi.riista.mobile.utils.LocaleUtil
import fi.riista.mobile.utils.toVisibility
import javax.inject.Inject


class SettingsFragment : PageFragment() {

    @Inject
    internal lateinit var appSync: AppSync

    @Inject
    internal lateinit var syncConfig: SyncConfig

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        setupActionBar(R.layout.actionbar_settings, false)

        val privacyStatementButton = view.findViewById<MaterialButton>(R.id.settings_privacy_statement_btn)
        privacyStatementButton.setOnClickListener { displayPrivacyStatement() }

        val termsOfServiceButton = view.findViewById<MaterialButton>(R.id.settings_terms_of_service_btn)
        termsOfServiceButton.setOnClickListener { displayTermsOfService() }

        val accessibilityButton = view.findViewById<MaterialButton>(R.id.settings_accessibility_btn)
        accessibilityButton.setOnClickListener { displayAccessibility() }

        val licenseButton = view.findViewById<MaterialButton>(R.id.settings_licences_btn)
        licenseButton.setOnClickListener { displayLicensesDialog() }

        setupSyncMode(view)
        setupLanguageSelect(view)
        setupVersionInfo(view)
        setupServerAddress(view)

        return view
    }

    private fun setupSyncMode(view: View) {
        val syncGroup = view.findViewById<RadioGroup>(R.id.settings_sync_group)
        val manualBtn = view.findViewById<RadioButton>(R.id.settings_sync_manual_btn)
        val automaticBtn = view.findViewById<RadioButton>(R.id.settings_sync_auto_btn)

        syncGroup.check(if (syncConfig.isAutomatic()) automaticBtn.id else manualBtn.id)

        manualBtn.setOnClickListener {
            appSync.disableAutomaticSync()
        }

        automaticBtn.setOnClickListener {
            appSync.enableAutomaticSync()
        }
    }

    private fun setupLanguageSelect(view: View) {
        val languageGroup = view.findViewById<RadioGroup>(R.id.settings_language_group)
        val languageFIBtn = view.findViewById<RadioButton>(R.id.settings_language_finnish)
        val languageSVBtn = view.findViewById<RadioButton>(R.id.settings_language_swedish)
        val languageENBtn = view.findViewById<RadioButton>(R.id.settings_language_english)

        when (getLanguageCodeSetting(context)) {
            LANGUAGE_CODE_FI -> languageGroup.check(languageFIBtn.id)
            LANGUAGE_CODE_SV -> languageGroup.check(languageSVBtn.id)
            LANGUAGE_CODE_EN -> languageGroup.check(languageENBtn.id)
        }

        languageFIBtn.setOnClickListener {
            if (LANGUAGE_CODE_FI != getLanguageCodeSetting(context)) {
                onLanguageSelected(LANGUAGE_CODE_FI)
            }
        }

        languageSVBtn.setOnClickListener {
            if (LANGUAGE_CODE_SV != getLanguageCodeSetting(context)) {
                onLanguageSelected(LANGUAGE_CODE_SV)
            }
        }

        languageENBtn.setOnClickListener {
            if (LANGUAGE_CODE_EN != getLanguageCodeSetting(context)) {
                onLanguageSelected(LANGUAGE_CODE_EN)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupServerAddress(view: View) {
        val context = requireContext()
        val setupButton = view.findViewById<TextView>(R.id.settings_server_address)
        setupButton.visibility = BuildInfo.isTestBuild().toVisibility()

        if (BuildInfo.isTestBuild()) {
            setupButton.text = "Palvelimen osoite: ${AppConfig.getBaseAddress()}"
            setupButton.setOnClickListener {
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT
                input.setText(AppConfig.getBaseAddress())
                input.setPadding(resources.getDimension(R.dimen.padding_medium).toInt())
                input.setSelectAllOnFocus(true)
                val builder = AlertDialog.Builder(context)
                builder
                    .setTitle("Aseta palvelimen osoite")
                    .setView(input)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        var address = input.text.toString()
                        if (!address.startsWith("http")) {
                            address = "https://$address"
                        }
                        val confirmationBuilder = AlertDialog.Builder(context)
                        confirmationBuilder
                            .setMessage("Asetetaanko uudeksi osoitteeksi\n ${address}?\n Muutos tulee voimaan kun kirjaudut ulos ja käynnistät sovelluksen uudestaan.")
                            .setPositiveButton(R.string.yes) { _, _ ->
                                setServerBaseAddress(context, address)
                            }
                            .setNegativeButton(R.string.no) { _, _ ->
                                // Do nothing
                            }
                            .show()

                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        // Do nothing
                    }
                    .show()
            }
        }
    }

    private fun onLanguageSelected(selectedLanguage: String) {
        val languageSetting = getLanguageCodeSetting(context)

        if (selectedLanguage != languageSetting) {
            setLanguageCodeSetting(context, selectedLanguage)
            LocaleUtil.setupLocale(requireContext())
            (activity as MainActivity).restartActivity()
        }
    }

    private fun setupVersionInfo(view: View) {
        val versionName = RiistaSDK.versionInfo
            .appVersion.takeIf { it.isNotEmpty() } ?: resources.getString(R.string.unknown)

        val versionText = view.findViewById<TextView>(R.id.settings_version_value)
        versionText.text = versionName
    }

    private fun displayLicensesDialog() {
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.licences_dialog_title))
        startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
    }

    private fun displayPrivacyStatement() {
        val languageCode = getLanguageCodeSetting(context)

        val privacyStatementUrl = ExternalUrls.getPrivacyStatementUrl(languageCode)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyStatementUrl)))
    }

    private fun displayAccessibility() {
        val languageCode = getLanguageCodeSetting(context)

        val accessibilityUrl = ExternalUrls.getAccessibilityUrl(languageCode)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(accessibilityUrl)))
    }

    private fun displayTermsOfService() {
        val languageCode = getLanguageCodeSetting(context)

        val termsOfServiceUrl = ExternalUrls.getTermsOfServiceUrl(languageCode)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(termsOfServiceUrl)))
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
