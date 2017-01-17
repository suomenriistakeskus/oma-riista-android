package fi.riista.mobile.pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fi.riista.mobile.R;
import fi.riista.mobile.activity.BaseActivity;
import fi.riista.mobile.activity.MainActivity;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.utils.AppPreferences;
import fi.vincit.androidutilslib.util.ViewAnnotations;

public class SettingsFragment extends PageFragment {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ViewAnnotations.apply(this, view);

        setupSyncMode(view);
        setupLanguageSelect(getActivity(), view);
        setupMapSourceSelect(getActivity(), view);
        setupVersionInfo(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setViewTitle(getString(R.string.title_settings));
    }

    void setupSyncMode(final View view) {

        RadioButton manual = (RadioButton) view.findViewById(R.id.manual);
        manual.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enableManualSync(view);
            }
        });
        RadioButton automatic = (RadioButton) view.findViewById(R.id.automatic);
        automatic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enableAutomaticSync(view);
            }
        });

        GameDatabase.SyncMode mode = GameDatabase.getInstance().getSyncMode(getActivity());
        if (mode == GameDatabase.SyncMode.SYNC_MANUAL) {
            manual.setChecked(true);
        } else {
            automatic.setChecked(true);
        }
    }

    void setupMapSourceSelect(final Context context, final View view) {
        RadioButton buttonGoogle = (RadioButton) view.findViewById(R.id.map_type_google_button);
        buttonGoogle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppPreferences.setMapTileSource(context, AppPreferences.MapTileSource.GOOGLE);
            }
        });
        RadioButton buttonMml = (RadioButton) view.findViewById(R.id.map_type_mml_button);
        buttonMml.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppPreferences.setMapTileSource(context, AppPreferences.MapTileSource.MML_TOPOGRAPHIC);
            }
        });

        AppPreferences.MapTileSource selectedTileSource = AppPreferences.getMapTileSource(context);
        if (selectedTileSource == AppPreferences.MapTileSource.GOOGLE) {
            buttonGoogle.setChecked(true);
        } else {
            buttonMml.setChecked(true);
        }
    }

    void setupLanguageSelect(final Context context, final View view) {
        AppCompatSpinner spinner = (AppCompatSpinner) view.findViewById(R.id.languageSpinner);

        List<String> selections = new ArrayList<>();
        selections.add(getResources().getString(R.string.Finnish));
        selections.add(getResources().getString(R.string.Swedish));
        selections.add(getResources().getString(R.string.English));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), R.layout.setting_spinner_item, selections);
        adapter.setDropDownViewResource(R.layout.setting_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        String languageSetting = AppPreferences.getLanguageCodeSetting(context);
        switch (languageSetting) {
            case AppPreferences.LANGUAGE_CODE_FI:
                spinner.setSelection(0);
                break;
            case AppPreferences.LANGUAGE_CODE_SV:
                spinner.setSelection(1);
                break;
            default: // "en"
                spinner.setSelection(2);
                break;
        }

        spinner.setOnItemSelectedListener(new LanguageSelectedListener(context));
    }

    void setupVersionInfo(View view) {
        String versionName = getResources().getString(R.string.unknown);
        try {
            versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionName = getResources().getString(R.string.unknown);
        }
        TextView versionText = (TextView) view.findViewById(R.id.versionnumber);
        versionText.setText(versionName);
    }

    void enableManualSync(View view) {
        GameDatabase.getInstance().setSyncMode(((MainActivity) getActivity()).getWorkContext(), GameDatabase.SyncMode.SYNC_MANUAL);
    }

    void enableAutomaticSync(View view) {
        GameDatabase.getInstance().setSyncMode(((MainActivity) getActivity()).getWorkContext(), GameDatabase.SyncMode.SYNC_AUTOMATIC);
    }

    @ViewAnnotations.ViewOnClick(R.id.licences_button)
    protected void onEditDateButtonClicked(View view) {
        displayLicensesDialog();
    }

    private void displayLicensesDialog() {
        @SuppressLint("InflateParams") // Null root param is ok for dialog
        WebView view = (WebView) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_licenses, null);
        view.loadUrl("file:///android_asset/open_source_licenses.html");
        new AlertDialog.Builder(getContext(), 0)
                .setTitle(getString(R.string.licences_dialog_title))
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private class LanguageSelectedListener implements AdapterView.OnItemSelectedListener {
        private Context mContext;

        LanguageSelectedListener(final Context context) {
            mContext = context;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    onLanguageSelected(AppPreferences.LANGUAGE_CODE_FI);
                    break;
                case 1:
                    onLanguageSelected(AppPreferences.LANGUAGE_CODE_SV);
                    break;
                case 2:
                default:
                    onLanguageSelected(AppPreferences.LANGUAGE_CODE_EN);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

        private void onLanguageSelected(String selectedLanguage) {
            String languageSetting = AppPreferences.getLanguageCodeSetting(mContext);

            if (!selectedLanguage.equals(languageSetting)) {
                AppPreferences.setLanguageCodeSetting(mContext, selectedLanguage);
                ((BaseActivity) getActivity()).setupLocaleFromSetting();
                ((MainActivity) getActivity()).restartActivity();
            }
        }
    }
}
