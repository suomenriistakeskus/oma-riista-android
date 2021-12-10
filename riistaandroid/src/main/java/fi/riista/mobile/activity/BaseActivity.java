package fi.riista.mobile.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import fi.riista.mobile.LocationClient;
import fi.riista.mobile.LocationClientProvider;
import fi.riista.mobile.R;
import fi.riista.mobile.RiistaApplication;
import fi.riista.mobile.network.CheckVersionTask;
import fi.riista.mobile.pages.PageFragment;
import fi.riista.mobile.ui.UpdateAvailableDialog;
import fi.riista.mobile.utils.KeyboardUtils;
import fi.riista.mobile.utils.LocaleUtil;
import fi.riista.mobile.utils.Utils;
import fi.vincit.androidutilslib.context.WorkContext;
import fi.vincit.androidutilslib.context.WorkContextProvider;
import fi.vincit.androidutilslib.graphics.UnitConverter;

import static java.util.Objects.requireNonNull;

public abstract class BaseActivity extends AppCompatActivity
        implements WorkContextProvider, PageFragment.OnFragmentInteractionListener, LocationClientProvider {

    private WorkContext mWorkContext;
    private View mTitleView;

    private boolean mIsVisible;

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(LocaleUtil.setupLocale(base));
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // Create the WorkContext before calling super so that restored fragments etc. can use it.
        mWorkContext = new WorkContext();
        mWorkContext.onCreateGlobal(this);

        LocaleUtil.setupLocale(this);

        super.onCreate(savedInstanceState);

        final ActionBar actionBar = requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        mTitleView = getLayoutInflater().inflate(R.layout.actionbar, null);
        actionBar.setCustomView(mTitleView);

        // Custom view does not get centered if just inflating view
        final View v = actionBar.getCustomView();
        final ViewGroup.LayoutParams lp = v.getLayoutParams();
        lp.width = ActionBar.LayoutParams.MATCH_PARENT;
        v.setLayoutParams(lp);

        setCustomTitle(getString(R.string.app_name));

        if (!RiistaApplication.sDidCheckVersion) {
            RiistaApplication.sDidCheckVersion = true;
            checkVersionUpdate();
        }
    }

    @Override
    protected void onResume() {
        // Needed because of locked screen orientation and because the application is handling screen orientation
        // configuration change by itself. Without this, the locale can change to system default. The issue is
        // effective in case (1) the system locale is different from what is set in the application and (2) screen
        // orientation is changed in another application after which this application is put again in foreground.
        LocaleUtil.setupLocale(this);

        super.onResume();

        mWorkContext.onResume();
        mIsVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        mWorkContext.onPause();
        mIsVisible = false;
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
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtil.setupLocale(this);
    }

    /**
     * @return Reusable location client fragment instance
     */
    @Override
    @NotNull
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
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        if (id == android.R.id.home) {
            KeyboardUtils.hideSoftKeyboard(this);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setCustomTitle(final String title) {
        final TextView textView = mTitleView.findViewById(R.id.title);
        textView.setText(title);
    }

    public void checkVersionUpdate() {
        // Do not care if check succeeds or fails. Try again after next app launch
        RiistaApplication.sDidCheckVersion = true;
        Log.d(Utils.LOG_TAG, "Checking if application update is available...");

        final CheckVersionTask task = new CheckVersionTask(getWorkContext()) {
            @Override
            protected void onFinishText(final String platformVersionsText) {
                Log.d(Utils.LOG_TAG, "Application version check finished " + platformVersionsText);

                if (Utils.shouldDisplayVersionUpdateDialog(platformVersionsText)) {
                    UpdateAvailableDialog.show(BaseActivity.this, "", 0);
                }
            }

            @Override
            protected void onError() {
                Log.d(Utils.LOG_TAG, "Error while checking application version");
            }
        };
        task.start();
    }

    public boolean isVisible() {
        return mIsVisible;
    }
}
