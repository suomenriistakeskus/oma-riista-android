package fi.riista.mobile.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import fi.riista.mobile.AppConfig;
import fi.riista.mobile.NetworkConnectivityReceiver;
import fi.riista.mobile.R;
import fi.riista.mobile.database.DiarySync.DiarySyncContext;
import fi.riista.mobile.database.GameDatabase;
import fi.riista.mobile.database.PermitManager;
import fi.riista.mobile.database.SpeciesInformation;
import fi.riista.mobile.pages.AnnouncementsFragment;
import fi.riista.mobile.pages.ContactDetailsFragment;
import fi.riista.mobile.pages.GameLogFragment;
import fi.riista.mobile.pages.HomeViewFragment;
import fi.riista.mobile.pages.MyDetailsFragment;
import fi.riista.mobile.pages.SettingsFragment;
import fi.riista.mobile.utils.AppPreferences;
import fi.riista.mobile.utils.Utils;

public class MainActivity extends BaseActivity implements DiarySyncContext {

    private GameDatabase mDatabase;

    private BroadcastReceiver mConnectivityReceiver = null;

    private BottomBar mBottomNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeAsUpIndicator(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_main);

        initDatabase();

        replacePageFragment(HomeViewFragment.newInstance());
        setupNavigationView();

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            createGPSPrompt();
        }

        SpeciesInformation.refreshInfo(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mConnectivityReceiver = new NetworkConnectivityReceiver(getWorkContext());
        registerReceiver(mConnectivityReceiver, intentFilter);

        if (mDatabase.getSyncMode(this) == GameDatabase.SyncMode.SYNC_AUTOMATIC) {
            mDatabase.initSync(getWorkContext(), 500);
        }

        if (getIntent().getBooleanExtra(LoginActivity.SHOW_ANNOUNCEMENTS_EXTRA, false)) {
            getIntent().removeExtra(LoginActivity.SHOW_ANNOUNCEMENTS_EXTRA);
            mBottomNavigationView.selectTabWithId(R.id.menu_announcements);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDatabase.stopSyncing();

        if (mConnectivityReceiver != null) {
            unregisterReceiver(mConnectivityReceiver);
        }

        mDatabase.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Handle resuming sync when returning to app
        if (mDatabase.getSyncMode(this) == GameDatabase.SyncMode.SYNC_AUTOMATIC) {
            mDatabase.initSync(getWorkContext(), 500);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //savedInstanceState.putInt("menuitem", mSelectedItem.ordinal());
    }

    public void initDatabase() {
        mDatabase = GameDatabase.getInstance();
        mDatabase.load(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int selectedId = mBottomNavigationView.getCurrentTabId();

        if (selectedId == R.id.menu_main) {
            finish();
        } else {
            mBottomNavigationView.selectTabWithId(R.id.menu_main);
        }
    }

    private void setupNavigationView() {
        mBottomNavigationView = (BottomBar) findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                Fragment fragment = null;

                switch (tabId) {
                    case R.id.menu_main:
                        fragment = HomeViewFragment.newInstance();
                        break;
                    case R.id.menu_game_log:
                        fragment = GameLogFragment.newInstance();
                        break;
                    case R.id.menu_announcements:
                        fragment = AnnouncementsFragment.newInstance();
                        break;
                    case R.id.menu_my_details:
                        fragment = MyDetailsFragment.newInstance();
                        break;
                    case R.id.menu_more:
                        displayMorePopupMenu();
                        break;
                }

                replacePageFragment(fragment);
            }
        });
        mBottomNavigationView.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.menu_more:
                        displayMorePopupMenu();
                        break;
                }
            }
        });
    }

    private void displayMorePopupMenu() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, mBottomNavigationView.getTabWithId(R.id.menu_more));
        popupMenu.getMenuInflater().inflate(R.menu.main_navigation_more, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Fragment innerFragment = null;

                switch (item.getItemId()) {
                    case R.id.menu_contact:
                        innerFragment = ContactDetailsFragment.newInstance();
                        break;
                    case R.id.menu_settings:
                        innerFragment = SettingsFragment.newInstance();
                        break;
                    case R.id.menu_magazine:
                        Intent intent = new Intent(MainActivity.this, MagazineActivity.class);
                        if (AppPreferences.LANGUAGE_CODE_SV.equalsIgnoreCase(AppPreferences.getLanguageCodeSetting(MainActivity.this))) {
                            intent.putExtra(MagazineActivity.EXTRA_URL, AppConfig.MAGAZINE_URL_SV);
                        }
                        else {
                            intent.putExtra(MagazineActivity.EXTRA_URL, AppConfig.MAGAZINE_URL_FI);
                        }
                        startActivity(intent);
                        break;
                    case R.id.menu_logout:
                        confirmLogout();
                        break;
                }

                replacePageFragment(innerFragment);
                return true;
            }
        });

        popupMenu.setGravity(Gravity.RIGHT);
        popupMenu.show();
    }

    void replacePageFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.commit();
        } else {
            Utils.LogMessage("Fragment null");
        }
    }

    private void selectItem(int position) {
        mBottomNavigationView.selectTabAtPosition(position);
    }

    public void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.logout) + "?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User canceled. Do nothing
                    }
                });
        builder.show();
    }

    public void logout() {
        mDatabase.stopSyncing();
        mDatabase.clearUpdateTimes();
        mDatabase.removeCredentials(this);
        AppPreferences.setUserInfo(this, null);
        PermitManager.getInstance(this).clearPermits();
        Utils.unregisterNotificationsAsync();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void createGPSPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.gps_prompt));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void syncStarted() {
        setSyncNotification(true);
    }

    @Override
    public void syncCompleted() {
        setSyncNotification(false);
    }

    /**
     * Use to recreate activity to apply updated language setting
     */
    public void restartActivity() {
        Intent intent = getIntent();
        mDatabase.close();
        finish();
        startActivity(intent);
    }
}
