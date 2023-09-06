package fi.riista.mobile.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.AndroidInjection
import fi.riista.mobile.R
import fi.riista.mobile.adapter.ShootingTestTabPagerAdapter
import fi.riista.mobile.pages.ShootingTestCalendarEventListFragment
import fi.riista.mobile.utils.LocaleUtil.localeFromLanguageSetting
import fi.riista.mobile.utils.LocaleUtil.setupLocale
import fi.riista.mobile.viewmodel.ShootingTestMainViewModel
import java.util.*
import javax.inject.Inject

class ShootingTestMainActivity : AppCompatActivity() {
    @JvmField
    @Inject
    var viewModelFactory: ViewModelProvider.Factory? = null
    private lateinit var viewModel: ShootingTestMainViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var optionsMenu: Menu
    private var refreshMenuItem: MenuItem? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(setupLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setupLocaleFromSetting()
        setContentView(R.layout.activity_shooting_test_main)
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setupViewPager()
        val intent = intent
        val calendarEventId = intent.getLongExtra(ShootingTestCalendarEventListFragment.EXTRA_CALENDAR_EVENT_ID, -1)
        val rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh)
        rotation.repeatCount = Animation.INFINITE
        viewModel = ViewModelProvider(this, viewModelFactory!!)[ShootingTestMainViewModel::class.java]
        viewModel.calendarEventId = calendarEventId
        viewModel.isOngoing.observe(this) { enabled: Boolean? ->
            refreshEnabledTabs(
                enabled,
                viewModel.isUserSelectedOfficial.value,
                viewModel.isUserCoordinator.value
            )
        }
        viewModel.isUserSelectedOfficial.observe(this) { selected: Boolean? ->
            refreshEnabledTabs(
                viewModel.isOngoing.value, selected, viewModel.isUserCoordinator.value
            )
        }
        viewModel.isUserCoordinator.observe(this) { coordinator: Boolean? ->
            refreshEnabledTabs(
                viewModel.isOngoing.value, viewModel.isUserSelectedOfficial.value, coordinator
            )
        }
        viewModel.noAttemptsCount.observe(this) { noAttemptsCount: Int? ->
            updateTabBadge(
                ShootingTestTabPagerAdapter.TAB_INDEX_QUEUE,
                noAttemptsCount ?: 0
            )
        }
        viewModel.noPaymentCount.observe(this) { noPaymentCount: Int? ->
            updateTabBadge(
                ShootingTestTabPagerAdapter.TAB_INDEX_PAYMENTS,
                noPaymentCount ?: 0
            )
        }
        viewModel.refreshing.observe(this) { aCount: Int? ->
            refreshMenuItem?.let { menuItem ->
                if (aCount != null && aCount > 0  && menuItem.isEnabled) {
                    menuItem.isEnabled = false
                    menuItem.actionView?.startAnimation(rotation)
                } else {
                    menuItem.isEnabled = true
                    menuItem.actionView?.clearAnimation()
                }
            }
        }
    }

    private fun setupViewPager() {
        val viewPagerAdapter = ShootingTestTabPagerAdapter(this)
        viewPager = findViewById(R.id.shooting_test_tab_content_pager)
        viewPager.adapter = viewPagerAdapter
        tabLayout = findViewById(R.id.tab)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = viewPagerAdapter.getPageTitle(position)
        }.attach()
        tabLayout.getTabAt(ShootingTestTabPagerAdapter.TAB_INDEX_QUEUE)?.setCustomView(R.layout.view_tab_with_badge)
        tabLayout.getTabAt(ShootingTestTabPagerAdapter.TAB_INDEX_PAYMENTS)?.setCustomView(R.layout.view_tab_with_badge)
    }

    private fun refreshEnabledTabs(
        ongoing: Boolean?,
        isUserSelected: Boolean?,
        isUserCoordinator: Boolean?
    ) {
        val paramsNonNull = ongoing != null && isUserSelected != null && isUserCoordinator != null
        val tabStrip = tabLayout.getChildAt(0) as LinearLayout
        tabStrip.getChildAt(ShootingTestTabPagerAdapter.TAB_INDEX_REGISTER).isClickable =
            paramsNonNull && ongoing!! && (isUserSelected!! || isUserCoordinator!!)
        tabStrip.getChildAt(ShootingTestTabPagerAdapter.TAB_INDEX_QUEUE).isClickable =
            paramsNonNull && ongoing!! && (isUserSelected!! || isUserCoordinator!!)
        tabStrip.getChildAt(ShootingTestTabPagerAdapter.TAB_INDEX_PAYMENTS).isClickable =
            paramsNonNull && (isUserSelected!! || isUserCoordinator!!)
        viewPager.isUserInputEnabled = paramsNonNull && ongoing!! && (isUserSelected!! || isUserCoordinator!!)
    }

    private fun updateTabBadge(tabIndex: Int, count: Int) {
        val tab = tabLayout.getTabAt(tabIndex)
        tab?.customView?.findViewById<TextView>(R.id.tab_badge)?.let { badge ->
            badge.visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
            badge.text = count.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_shooting_test_main, menu)
        optionsMenu = menu
        refreshMenuItem = menu.findItem(R.id.item_refresh)
        refreshMenuItem?.setActionView(R.layout.view_refresh_action_view)
        refreshMenuItem?.actionView?.setOnClickListener {
            refreshMenuItem?.itemId?.let { itemId ->
                optionsMenu.performIdentifierAction(itemId, 0)
            }
        }
        refreshMenuItem?.isEnabled = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.item_refresh -> {
                viewModel.refreshCalendarEvent()
                viewModel.refreshParticipants()
                return true
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupLocaleFromSetting() {
        val locale = localeFromLanguageSetting(this)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        val res = baseContext.resources
        res.updateConfiguration(config, res.displayMetrics)
    }
}
