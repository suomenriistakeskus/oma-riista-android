package fi.riista.mobile.pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.button.MaterialButton
import dagger.android.support.AndroidSupportInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.mobile.AppConfig
import fi.riista.mobile.ExternalUrls
import fi.riista.mobile.R
import fi.riista.mobile.activity.MainActivity
import fi.riista.mobile.activity.MapSettingsActivity
import fi.riista.mobile.feature.harvest.HarvestSettingsActivity
import fi.riista.mobile.feature.unregister.UnregisterUserAccountActivity
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.sync.SyncConfig
import fi.riista.mobile.sync.SyncMode
import fi.riista.mobile.utils.AppPreferences.*
import fi.riista.mobile.utils.BuildInfo
import fi.riista.mobile.utils.Constants
import fi.riista.mobile.utils.LocaleUtil
import fi.riista.mobile.utils.openInBrowser
import fi.riista.mobile.utils.toVisibility
import javax.inject.Inject


class SettingsFragment : PageFragment() {

    @Inject
    internal lateinit var appSync: AppSync

    @Inject
    internal lateinit var syncConfig: SyncConfig


    private var refreshItem: MenuItem? = null
    private var refreshAllItem: MenuItem? = null
    private val disposeBag = DisposeBag()

    // Dagger injection of a Fragment instance must be done in On-Attach lifecycle phase.
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        setupActionBar(R.layout.actionbar_settings, false)

        val harvestSettingsButton = view.findViewById<MaterialButton>(R.id.settings_harvest_settings_btn)
        harvestSettingsButton.setOnClickListener {
            val intent = Intent(activity, HarvestSettingsActivity::class.java)
            startActivity(intent)
        }
        val mapSettingsButton = view.findViewById<MaterialButton>(R.id.settings_map_settings_btn)
        mapSettingsButton.setOnClickListener {
            val intent = Intent(context, MapSettingsActivity::class.java)
            startActivity(intent)
        }

        val unregisterButton = view.findViewById<MaterialButton>(R.id.settings_unregister_account_btn)
        unregisterButton.setOnClickListener { displayUnregisterUserAccount() }

        setupSyncMode(view)
        setupLanguageSelect(view)
        setupVersionInfo(view)
        setupServerAddress(view)

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_refresh, menu)
        inflater.inflate(R.menu.menu_refresh_all, menu)
        super.onCreateOptionsMenu(menu, inflater)

        // Show/hide refresh button according to sync settings.
        refreshItem = menu.findItem(R.id.item_refresh)
        refreshAllItem = menu.findItem(R.id.item_refresh_all)
        updateManualSyncButtonIndicator(manualSyncPossible = appSync.manualSynchronizationPossible.value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_refresh -> {
                appSync.scheduleImmediateSync(forceUserContentSync = true, forceContentReload = false)
                true
            }
            R.id.item_refresh_all -> {
                appSync.scheduleImmediateSync(forceUserContentSync = true, forceContentReload = true)
                true
            }
            else -> false
        }
    }

    override fun onStart() {
        super.onStart()

        appSync.manualSynchronizationPossible.bindAndNotify { manualSyncPossible ->
            refreshItem?.let { item ->
                item.isEnabled = manualSyncPossible
                item.icon?.alpha = when (manualSyncPossible) {
                    true -> 255
                    false -> Constants.DISABLED_ALPHA
                }
            }
            refreshAllItem?.let { item ->
                item.isEnabled = manualSyncPossible
            }
        }.disposeBy(disposeBag)
    }

    override fun onStop() {
        super.onStop()
        disposeBag.disposeAll()
    }

    private fun updateManualSyncButtonIndicator(manualSyncPossible: Boolean) {
        refreshItem?.let { item ->
            item.isVisible = syncConfig.syncMode == SyncMode.SYNC_MANUAL
            item.isEnabled = manualSyncPossible
            item.icon?.alpha = when (manualSyncPossible) {
                true -> 255
                false -> Constants.DISABLED_ALPHA
            }
        }
        refreshAllItem?.let { item ->
            item.isVisible = syncConfig.syncMode == SyncMode.SYNC_MANUAL
            item.isEnabled = manualSyncPossible
        }
    }

    private fun setupSyncMode(view: View) {
        val syncGroup = view.findViewById<RadioGroup>(R.id.settings_sync_group)
        val manualBtn = view.findViewById<RadioButton>(R.id.settings_sync_manual_btn)
        val automaticBtn = view.findViewById<RadioButton>(R.id.settings_sync_auto_btn)

        syncGroup.check(if (syncConfig.isAutomatic()) automaticBtn.id else manualBtn.id)

        manualBtn.setOnClickListener {
            appSync.disableAutomaticSync()
            activity?.invalidateOptionsMenu()
        }

        automaticBtn.setOnClickListener {
            appSync.enableAutomaticSync()
            activity?.invalidateOptionsMenu()
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
                AlertDialog.Builder(context)
                    .setTitle("Aseta palvelimen osoite")
                    .setView(input)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        var address = input.text.toString()
                        if (!address.startsWith("http")) {
                            address = "https://$address"
                        }
                        AlertDialog.Builder(context)
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

    private fun displayUnregisterUserAccount() {
        startActivity(UnregisterUserAccountActivity.getLaunchIntent(requireActivity()))
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
