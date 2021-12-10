package fi.riista.mobile.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import fi.riista.mobile.R;
import fi.riista.mobile.adapter.ShootingTestTabPagerAdapter;
import fi.riista.mobile.pages.ShootingTestCalendarEventListFragment;
import fi.riista.mobile.pages.ShootingTestTabContentFragment;
import fi.riista.mobile.ui.ShootingTestTabViewPager;
import fi.riista.mobile.utils.LocaleUtil;
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel;

import static fi.riista.mobile.adapter.ShootingTestTabPagerAdapter.TAB_INDEX_PAYMENTS;
import static fi.riista.mobile.adapter.ShootingTestTabPagerAdapter.TAB_INDEX_QUEUE;
import static fi.riista.mobile.adapter.ShootingTestTabPagerAdapter.TAB_INDEX_REGISTER;

public class ShootingTestMainActivity extends AppCompatActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private ShootingTestMainViewModel mModel;

    private TabLayout mTabLayout;
    private ShootingTestTabViewPager mViewPager;

    private Menu mMenu;
    private MenuItem mRefreshMenuItem;

    @Override
    protected void attachBaseContext(final Context newBase) {
        super.attachBaseContext(LocaleUtil.setupLocale(newBase));
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        setupLocaleFromSetting();

        setContentView(R.layout.activity_shooting_test_main);

        final Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupViewPager();

        final Intent intent = getIntent();
        final long calendarEventId =
                intent.getLongExtra(ShootingTestCalendarEventListFragment.EXTRA_CALENDAR_EVENT_ID, -1);

        final Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
        rotation.setRepeatCount(Animation.INFINITE);

        mModel = new ViewModelProvider(this, viewModelFactory).get(ShootingTestMainViewModel.class);
        mModel.setCalendarEventId(calendarEventId);


        mModel.isOngoing().observe(this, enabled -> {
            refreshEnabledTabs(enabled, mModel.isUserSelectedOfficial().getValue(), mModel.isUserCoordinator().getValue());
        });
        mModel.isUserSelectedOfficial().observe(this, selected -> {
            refreshEnabledTabs(mModel.isOngoing().getValue(), selected, mModel.isUserCoordinator().getValue());
        });
        mModel.isUserCoordinator().observe(this, coordinator -> {
            refreshEnabledTabs(mModel.isOngoing().getValue(), mModel.isUserSelectedOfficial().getValue(), coordinator);
        });

        mModel.getNoAttemptsCount().observe(this, noAttemptsCount -> {
            updateTabBadge(TAB_INDEX_QUEUE, noAttemptsCount != null ? noAttemptsCount : 0);
        });

        mModel.getNoPaymentCount().observe(this, noPaymentCount -> {
            updateTabBadge(TAB_INDEX_PAYMENTS, noPaymentCount != null ? noPaymentCount : 0);
        });

        mModel.getRefreshing().observe(this, aCount -> {
            if (mRefreshMenuItem != null) {
                if (aCount != null && aCount > 0 && mRefreshMenuItem.isEnabled()) {
                    mRefreshMenuItem.setEnabled(false);
                    mRefreshMenuItem.getActionView().startAnimation(rotation);
                } else {
                    mRefreshMenuItem.setEnabled(true);
                    mRefreshMenuItem.getActionView().clearAnimation();
                }
            }
        });
    }

    private void setupViewPager() {
        final ShootingTestTabPagerAdapter viewPagerAdapter = new ShootingTestTabPagerAdapter(getSupportFragmentManager(), this);

        mViewPager = findViewById(R.id.shooting_test_tab_content_pager);
        mViewPager.setAdapter(viewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                final ShootingTestTabContentFragment fragment = (ShootingTestTabContentFragment) viewPagerAdapter.getItem(position);
                fragment.onTabSelected();
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
            }
        });

        mTabLayout = findViewById(R.id.tab);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(TAB_INDEX_QUEUE).setCustomView(R.layout.view_tab_with_badge);
        mTabLayout.getTabAt(TAB_INDEX_PAYMENTS).setCustomView(R.layout.view_tab_with_badge);
    }

    private void refreshEnabledTabs(@Nullable final Boolean ongoing,
                                    @Nullable final Boolean isUserSelected,
                                    @Nullable final Boolean isUserCoordinator) {

        final boolean paramsNonNull = ongoing != null && isUserSelected != null && isUserCoordinator != null;

        final LinearLayout tabStrip = ((LinearLayout) mTabLayout.getChildAt(0));
        tabStrip.getChildAt(TAB_INDEX_REGISTER).setClickable(paramsNonNull && ongoing && (isUserSelected || isUserCoordinator));
        tabStrip.getChildAt(TAB_INDEX_QUEUE).setClickable(paramsNonNull && ongoing && (isUserSelected || isUserCoordinator));
        tabStrip.getChildAt(TAB_INDEX_PAYMENTS).setClickable(paramsNonNull && (isUserSelected || isUserCoordinator));

        mViewPager.setEnableSwipe(paramsNonNull && ongoing && (isUserSelected || isUserCoordinator));
    }

    private void updateTabBadge(final int tabIndex, final int count) {
        final TabLayout.Tab tab = mTabLayout.getTabAt(tabIndex);

        final TextView badge = tab.getCustomView().findViewById(R.id.tab_badge);
        badge.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        badge.setText(String.valueOf(count));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_shooting_test_main, menu);

        mMenu = menu;
        mRefreshMenuItem = menu.findItem(R.id.item_refresh);
        mRefreshMenuItem.setActionView(R.layout.view_refresh_action_view);
        mRefreshMenuItem.getActionView().setOnClickListener(v -> mMenu.performIdentifierAction(mRefreshMenuItem.getItemId(), 0));
        mRefreshMenuItem.setEnabled(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.item_refresh:
                mModel.refreshCalendarEvent();
                mModel.refreshParticipants();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setupLocaleFromSetting() {
        final Locale locale = LocaleUtil.localeFromLanguageSetting(this);
        Locale.setDefault(locale);

        final Configuration config = new Configuration();
        config.locale = locale;

        final Resources res = getBaseContext().getResources();
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}
