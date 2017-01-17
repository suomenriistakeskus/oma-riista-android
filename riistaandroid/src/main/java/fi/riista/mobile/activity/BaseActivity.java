package fi.riista.mobile.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import fi.riista.mobile.LocationClient;
import fi.riista.mobile.R;
import fi.riista.mobile.pages.PageFragment;
import fi.riista.mobile.utils.AppPreferences;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.context.WorkContextProvider;
import fi.vincit.androidutilslib.graphics.UnitConverter;

public class BaseActivity extends AppCompatActivity implements WorkContextProvider, PageFragment.OnFragmentInteractionListener {

    private WorkContext mWorkContext;
    private View mTitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Create the WorkContext before calling super so that restored fragments etc. can use it.
        mWorkContext = new WorkContext();
        mWorkContext.onCreateGlobal(this);

        setupLocaleFromSetting();

        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        mTitleView = getLayoutInflater().inflate(R.layout.actionbar, null);
        getSupportActionBar().setCustomView(mTitleView);

        // Custom view does not get centered if just inflating view
        View v = getSupportActionBar().getCustomView();
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        lp.width = ActionBar.LayoutParams.MATCH_PARENT;
        v.setLayoutParams(lp);

        // Extra padding should be calculated separately for views with/without options menu items
        int padding = Math.round(UnitConverter.dipToPx(this, 60));
        mTitleView.setPadding(mTitleView.getPaddingLeft(), mTitleView.getPaddingTop(), padding, mTitleView.getPaddingBottom());

        setCustomTitle(getString(R.string.app_name));
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWorkContext.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mWorkContext.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWorkContext.onDestroy();
    }

    @Override
    public WorkContext getWorkContext() {
        return mWorkContext;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setupLocaleFromSetting();
    }

    public void setupLocaleFromSetting() {
        String languageSelected = AppPreferences.getLanguageCodeSetting(this);
        Locale locale = new Locale(languageSelected);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    /**
     * @return Reusable location client fragment instance
     */
    public LocationClient getLocationClient() {
        LocationClient client = (LocationClient) getSupportFragmentManager().findFragmentByTag(LocationClient.LOCATION_CLIENT_TAG);

        if (client == null) {
            client = new LocationClient();
            getSupportFragmentManager().beginTransaction()
                    .add(client, LocationClient.LOCATION_CLIENT_TAG)
                    .commit();
        }

        return client;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setCustomTitle(String title) {
        TextView textView = (TextView) mTitleView.findViewById(R.id.title);
        textView.setText(title);
    }

    void setSyncNotification(boolean enabled) {
        ProgressBar progressBar = (ProgressBar) mTitleView.findViewById(R.id.main_progress_spinner);

        if (enabled) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
