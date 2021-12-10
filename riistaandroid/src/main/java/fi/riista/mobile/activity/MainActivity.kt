package fi.riista.mobile.activity

import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.AndroidInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.common.userInfo.LoginStatus
import fi.riista.mobile.ExternalUrls.Companion.getEventSearchUrl
import fi.riista.mobile.ExternalUrls.Companion.getHunterMagazineUrl
import fi.riista.mobile.ExternalUrls.Companion.getHuntingSeasonsUrl
import fi.riista.mobile.NetworkConnectivityReceiver
import fi.riista.mobile.R
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.database.PermitManager
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.feature.groupHunting.GroupHuntingActivity
import fi.riista.mobile.pages.*
import fi.riista.mobile.pages.MapViewer.FullScreenExpand
import fi.riista.mobile.sync.AnnouncementSync
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.sync.AppSync.AppSyncListener
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.CredentialsStore
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : BaseActivity(), AppSyncListener, FullScreenExpand {
    @Inject
    lateinit var harvestDatabase: HarvestDatabase

    @Inject
    lateinit var permitManager: PermitManager

    @Inject
    lateinit var appSync: AppSync

    @Inject
    lateinit var announcementsSync: AnnouncementSync

    @Inject
    lateinit var credentialsStore: CredentialsStore

    @Inject
    lateinit var userInfoStore: UserInfoStore

    private var connectivityReceiver: BroadcastReceiver? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var progressBar: ProgressBar
    private var syncOnResume = true
    private var groupHuntingAvailable = false
    private val disposeBag = DisposeBag()

    public override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        supportActionBar?.setHomeAsUpIndicator(ColorDrawable(Color.TRANSPARENT))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_main)
        progressBar = findViewById(R.id.progress_horizontal)
        checkIntentForSyncExtra(intent)
        initDatabase()
        replacePageFragment(HomeViewFragment.newInstance())
        setupNavigationView()
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            createGPSPrompt()
        }
        SpeciesInformation.refreshInfo(this)

        // checkIntentForSyncExtra() must be called before registerConnectivityReceiver() because
        // syncOnResume is set in the former method.
        registerConnectivityReceiver()
        checkIntentForAnnouncementExtra(intent)
        registerLoginStatus()
        registerGroupHuntingStatus()
    }

    private fun registerLoginStatus() {
        RiistaSDK.currentUserContext.loginStatus.bindAndNotify { loginStatus ->
            if (loginStatus is LoginStatus.LoggedIn) {
                checkGroupHuntingAvailability()
            }
        }.disposeBy(disposeBag)
    }

    private fun registerGroupHuntingStatus() {
        RiistaSDK.currentUserContext.groupHuntingContext
            .clubContextsProvider.loadStatus.bindAndNotify {
                groupHuntingAvailable = RiistaSDK.currentUserContext.groupHuntingContext.groupHuntingAvailable
            }
            .disposeBy(disposeBag)
    }

    private fun checkGroupHuntingAvailability() {
        CoroutineScope(Dispatchers.Main).launch {
            RiistaSDK.currentUserContext.groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
    }

    private fun checkIntentForSyncExtra(intent: Intent) {
        val doNotInitiateSync = intent.getBooleanExtra(DO_NOT_INITIATE_SYNC, false)
        syncOnResume = !doNotInitiateSync
    }

    private fun initDatabase() {
        val credentials = credentialsStore.get()
        val username = credentials?.username
        harvestDatabase.setUser(username)
    }

    fun replacePageFragment(fragment: Fragment?) {
        if (fragment != null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.content_frame, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        } else {
            Utils.LogMessage("Fragment null")
        }
    }

    private fun setupNavigationView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem: MenuItem ->
            var fragment: Fragment? = null
            when (menuItem.itemId) {
                R.id.menu_home -> fragment = HomeViewFragment.newInstance()
                R.id.menu_game_log -> fragment = GameLogFragment.newInstance()
                R.id.menu_map -> {
                    fragment = MapViewer.newInstance(enablePois = true)
                    // Map fragment is used in many places with different titles, so set the title manually.
                    setCustomTitle(getString(R.string.map_title))
                }
                R.id.menu_announcements -> fragment = AnnouncementsFragment.newInstance(true)
                R.id.menu_more -> {
                }
            }
            replacePageFragment(fragment)
            true
        }
        bottomNavigationView.menu.getItem(MORE_ITEM_INDEX).setOnMenuItemClickListener {
            displayMorePopupMenu()
            true
        }
    }

    private fun displayMorePopupMenu() {
        selectItem(R.id.menu_more)
        val popupMenu = PopupMenu(this@MainActivity, bottomNavigationView)
        popupMenu.menuInflater.inflate(R.menu.menu_main_more, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            var innerFragment: Fragment? = null
            when (item.itemId) {
                R.id.menu_my_details -> innerFragment = MyDetailsFragment.newInstance()
                R.id.menu_gallery -> innerFragment = GalleryFragment.newInstance()
                R.id.menu_contact -> innerFragment = ContactDetailsFragment.newInstance()
                R.id.menu_settings -> innerFragment = SettingsFragment.newInstance()
                R.id.menu_shooting_test_list -> innerFragment = ShootingTestCalendarEventListFragment.newInstance()
                R.id.menu_hunting_group_leader -> {
                    startActivity(Intent(this, GroupHuntingActivity::class.java))
                }
                R.id.menu_event_search -> {
                    val eventSearchUrl = getEventSearchUrl(languageCode)
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(eventSearchUrl)))
                }
                R.id.menu_magazine -> {
                    val magazineUrl = getHunterMagazineUrl(languageCode)
                    val intent = Intent(this@MainActivity, MagazineActivity::class.java)
                    intent.putExtra(MagazineActivity.EXTRA_URL, magazineUrl)
                    startActivity(intent)
                }
                R.id.menu_hunting_seasons -> {
                    val huntingSeasonsUrl = getHuntingSeasonsUrl(languageCode)
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(huntingSeasonsUrl)))
                }
                R.id.menu_logout -> confirmLogout()
            }
            replacePageFragment(innerFragment)
            true
        }
        popupMenu.menu.findItem(R.id.menu_hunting_group_leader).isVisible = groupHuntingAvailable
        val userInfo = userInfoStore.getUserInfo()
        val displayShootingTests = userInfo != null && userInfo.enableShootingTests
        popupMenu.menu.findItem(R.id.menu_shooting_test_list).isVisible = displayShootingTests
        popupMenu.gravity = Gravity.END
        popupMenu.show()
    }

    private val languageCode: String
        get() = AppPreferences.getLanguageCodeSetting(this)

    fun selectItem(id: Int) {
        bottomNavigationView.menu.findItem(id).isChecked = true
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.logout) + "?")
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int -> logout() }
                .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
                .show()
    }

    private fun logout() {
        RiistaSDK.logout()

        appSync.stopAutomaticSync()
        harvestDatabase.clearUpdateTimes()
        credentialsStore.clear()
        AppPreferences.clearAll(this)
        permitManager.clearPermits()
        Utils.unregisterNotificationsAsync()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun createGPSPrompt() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.gps_prompt))
                .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton(R.string.no) { _: DialogInterface?, _: Int -> }
                .create()
                .show()
    }

    private fun registerConnectivityReceiver() {
        if (syncOnResume) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            connectivityReceiver = NetworkConnectivityReceiver(appSync)
            registerReceiver(connectivityReceiver, intentFilter)
        }
    }

    private fun checkIntentForAnnouncementExtra(intent: Intent) {
        if (intent.getBooleanExtra(LoginActivity.SHOW_ANNOUNCEMENTS_EXTRA, false)) {
            intent.removeExtra(LoginActivity.SHOW_ANNOUNCEMENTS_EXTRA)
            announcementsSync.sync {

                // A state check is needed because user might switch to another Activity before
                // sync task finishes.
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    replacePageFragment(AnnouncementsFragment.newInstance(false))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntentForSyncExtra(intent)
        registerConnectivityReceiver()
        checkIntentForAnnouncementExtra(intent)
    }

    override fun onStart() {
        super.onStart()
        appSync.addSyncListener(this)
    }

    override fun onResume() {
        super.onResume()

        // Handle resuming sync when returning to app.
        if (syncOnResume) {
            appSync.initAutomaticSync(500)
        }
    }

    override fun onStop() {
        super.onStop()
        appSync.removeSyncListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        appSync.stopAutomaticSync()
        if (connectivityReceiver != null) {
            unregisterReceiver(connectivityReceiver)
        }
        harvestDatabase.close()

        disposeBag.disposeAll()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val selectedId = bottomNavigationView.selectedItemId
        if (selectedId == R.id.menu_home) {
            finish()
        } else {
            bottomNavigationView.selectedItemId = R.id.menu_home
        }
    }

    override fun setFullscreenMode(fullscreen: Boolean) {
        val attrs = window.attributes
        val actionBar = supportActionBar
        if (fullscreen) {
            attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            bottomNavigationView.visibility = View.GONE
            actionBar?.hide()
        } else {
            attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            bottomNavigationView.visibility = View.VISIBLE
            actionBar?.show()
        }
        window.attributes = attrs
    }

    override fun onSyncStarted() {
        progressBar.visibility = View.VISIBLE
    }

    override fun onSyncCompleted() {
        progressBar.visibility = View.GONE
    }

    /**
     * Use to recreate activity to apply updated language setting.
     */
    fun restartActivity() {
        val intent = intent
        harvestDatabase.close()
        finish()
        startActivity(intent)
    }

    companion object {
        const val DO_NOT_INITIATE_SYNC = "doNotInitiateSync"
        private const val MORE_ITEM_INDEX = 4
    }
}
