package fi.riista.mobile.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.AndroidInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.domain.userInfo.LoginStatus
import fi.riista.common.reactive.AppObservable
import fi.riista.common.reactive.DisposeBag
import fi.riista.common.reactive.disposeBy
import fi.riista.mobile.ExternalUrls.Companion.getEventSearchUrl
import fi.riista.mobile.ExternalUrls.Companion.getHuntingSeasonsUrl
import fi.riista.mobile.NetworkConnectivityReceiver
import fi.riista.mobile.R
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.database.HarvestDatabaseMigrationToRiistaSDK
import fi.riista.mobile.database.SpeciesInformation
import fi.riista.mobile.feature.groupHunting.GroupHuntingActivity
import fi.riista.mobile.feature.huntingControl.HuntingControlActivity
import fi.riista.mobile.feature.login.LoginActivity
import fi.riista.mobile.feature.moreView.MoreItemType
import fi.riista.mobile.feature.moreView.MoreViewFragment
import fi.riista.mobile.feature.unregister.UnregisterUserAccountActivityLauncher
import fi.riista.mobile.observation.ObservationDatabase
import fi.riista.mobile.observation.ObservationDatabaseMigrationToRiistaSDK
import fi.riista.mobile.pages.AnnouncementsFragment
import fi.riista.mobile.pages.ContactDetailsFragment
import fi.riista.mobile.pages.GalleryFragment
import fi.riista.mobile.pages.GameLogFragment
import fi.riista.mobile.pages.HomeViewFragment
import fi.riista.mobile.pages.MapViewer
import fi.riista.mobile.pages.MapViewer.FullScreenExpand
import fi.riista.mobile.pages.MyDetailsFragment
import fi.riista.mobile.pages.SettingsFragment
import fi.riista.mobile.pages.ShootingTestCalendarEventListFragment
import fi.riista.mobile.srva.SrvaDatabaseMigrationToRiistaSDK
import fi.riista.mobile.sync.AnnouncementSync
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.sync.AppSync.AppSyncListener
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.DelegatingAlertDialogListener
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.utils.AppPreferences
import fi.riista.mobile.utils.BackgroundOperationStatus
import fi.riista.mobile.utils.CredentialsStore
import fi.riista.mobile.utils.LogoutHelper
import fi.riista.mobile.utils.UserInfoStore
import fi.riista.mobile.utils.Utils
import fi.riista.mobile.utils.openInBrowser
import fi.riista.mobile.utils.toVisibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity
  : BaseActivity(),
    AppSyncListener,
    FullScreenExpand,
    MoreViewFragment.InteractionManager
{
    @Inject
    lateinit var harvestDatabase: HarvestDatabase

    @Inject
    lateinit var observationDatabase: ObservationDatabase

    @Inject
    lateinit var appSync: AppSync

    @Inject
    lateinit var logoutHelper: LogoutHelper

    @Inject
    lateinit var announcementsSync: AnnouncementSync

    @Inject
    lateinit var credentialsStore: CredentialsStore

    @Inject
    lateinit var userInfoStore: UserInfoStore

    @Inject
    lateinit var backgroundOperationStatus: BackgroundOperationStatus

    @Suppress("unused")
    private val dialogListener = DelegatingAlertDialogListener(this).apply {
        registerPositiveCallback(AlertDialogId.MAIN_ACTIVITY_LOGOUT_CONFIRMATION) {
            logout()
        }
        registerPositiveCallback(AlertDialogId.MAIN_ACTIVITY_GPS_PROMPT) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private var connectivityReceiver: BroadcastReceiver? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var progressBar: ProgressBar
    private var syncOnResume = true
    override var groupHuntingAvailable = AppObservable(false)
    override var huntingControlAvailable = AppObservable(false)
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
        replacePageFragment(HomeViewFragment())
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
        registerHuntingControlStatus()
        indicateBackgroundOperationStatus()
    }

    private fun registerLoginStatus() {
        RiistaSDK.currentUserContext.loginStatus.bindAndNotify { loginStatus ->
            if (loginStatus is LoginStatus.LoggedIn) {
                checkGroupHuntingAvailability()

                // ensure hunting control gets updated data from the backend by refreshing
                checkHuntingControlAvailability(refresh = true)

                UnregisterUserAccountActivityLauncher.launchIfAccountUnregistrationRequested(parentActivity = this)
            }
        }.disposeBy(disposeBag)
    }

    private fun registerGroupHuntingStatus() {
        RiistaSDK.currentUserContext.groupHuntingContext
            .clubContextsProvider.loadStatus.bindAndNotify {
                groupHuntingAvailable.set(RiistaSDK.currentUserContext.groupHuntingContext.groupHuntingAvailable)
            }
            .disposeBy(disposeBag)
    }

    private fun checkGroupHuntingAvailability() {
        CoroutineScope(Dispatchers.Main).launch {
            RiistaSDK.currentUserContext.groupHuntingContext.checkAvailabilityAndFetchClubs()
        }
    }

    private fun registerHuntingControlStatus() {
        RiistaSDK.huntingControlContext
            .huntingControlRhyProvider.loadStatus.bindAndNotify {
                huntingControlAvailable.set(RiistaSDK.huntingControlContext.huntingControlAvailable)
            }
            .disposeBy(disposeBag)
    }

    private fun indicateBackgroundOperationStatus() {
        backgroundOperationStatus.backgroundOperationInProgress
            .bindAndNotify { backgroundOperationInProgress ->
                progressBar.visibility = backgroundOperationInProgress.toVisibility()
            }
            .disposeBy(disposeBag)
    }

    private fun checkHuntingControlAvailability(refresh: Boolean = false) {
        CoroutineScope(Dispatchers.Main).launch {
            RiistaSDK.huntingControlContext.checkAvailability(refresh)
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

        // Run migrations from app databases to Riista SDK database
        backgroundOperationStatus.startOperation(BackgroundOperationStatus.Operation.DATABASE_MIGRATION)
        SrvaDatabaseMigrationToRiistaSDK.copyEvents {
            ObservationDatabaseMigrationToRiistaSDK.copyObservations(observationDatabase) {
                HarvestDatabaseMigrationToRiistaSDK.copyHarvests(harvestDatabase) {
                    backgroundOperationStatus.finishOperation(BackgroundOperationStatus.Operation.DATABASE_MIGRATION)
                    appSync.enableSyncPrecondition(AppSync.SyncPrecondition.DATABASE_MIGRATION_FINISHED)
                }
            }
        }
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
            val fragment = when (menuItem.itemId) {
                R.id.menu_home -> HomeViewFragment()
                R.id.menu_game_log -> GameLogFragment()
                R.id.menu_map -> createMapFragment()
                R.id.menu_announcements -> AnnouncementsFragment.newInstance(true)
                R.id.menu_more -> createMoreViewFragment()
                else -> null
            }
            replacePageFragment(fragment)
            true
        }
    }

    private fun createMoreViewFragment(): Fragment {
        checkGroupHuntingAvailability()
        checkHuntingControlAvailability()
        return MoreViewFragment()
    }

    private fun createMapFragment(): Fragment {
        val fragment = MapViewer.newInstance(enablePois = true)
        // Map fragment is used in many places with different titles, so set the title manually.
        setCustomTitle(getString(R.string.map_title))
        return fragment
    }

    override fun moreItemClicked(type: MoreItemType) {
        when (type) {
            MoreItemType.MY_DETAILS -> replacePageFragment(MyDetailsFragment.newInstance())
            MoreItemType.GALLERY -> replacePageFragment(GalleryFragment.newInstance())
            MoreItemType.CONTACT_DETAILS -> replacePageFragment(ContactDetailsFragment.newInstance())
            MoreItemType.SHOOTING_TESTS -> replacePageFragment(ShootingTestCalendarEventListFragment())
            MoreItemType.SETTINGS -> replacePageFragment(SettingsFragment.newInstance())
            MoreItemType.HUNTING_DIRECTOR -> startActivity(Intent(this, GroupHuntingActivity::class.java))
            MoreItemType.HUNTING_CONTROL -> startActivity(Intent(this, HuntingControlActivity::class.java))
            MoreItemType.EVENT_SEARCH -> Uri.parse(getEventSearchUrl(languageCode)).openInBrowser(this)
            MoreItemType.MAGAZINE -> startActivity(MagazineActivity.getLaunchIntent(this))
            MoreItemType.SEASONS -> Uri.parse(getHuntingSeasonsUrl(languageCode)).openInBrowser(this)
            MoreItemType.LOGOUT -> confirmLogout()
        }
    }

    private val languageCode: String
        get() = AppPreferences.getLanguageCodeSetting(this)

    fun selectItem(id: Int) {
        bottomNavigationView.menu.findItem(id).isChecked = true
    }

    private fun confirmLogout() {
        AlertDialogFragment.Builder(this, AlertDialogId.MAIN_ACTIVITY_LOGOUT_CONFIRMATION)
            .setMessage(getString(R.string.logout) + "?")
            .setPositiveButton(android.R.string.ok)
            .setNegativeButton(android.R.string.cancel)
            .build()
            .show(supportFragmentManager)
    }

    private fun logout() {
        logoutHelper.logout(context = this)

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun createGPSPrompt() {
        AlertDialogFragment.Builder(this, AlertDialogId.MAIN_ACTIVITY_GPS_PROMPT)
            .setMessage(getString(R.string.gps_prompt))
            .setPositiveButton(R.string.yes)
            .setNegativeButton(R.string.no)
            .build()
            .show(supportFragmentManager)
    }

    private fun registerConnectivityReceiver() {
        if (syncOnResume) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            Log.d("MainActivity", "Registering connectivity receiver")
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
        appSync.addSyncListener(listener = this, notifyImmediately = true)
    }

    override fun onResume() {
        super.onResume()

        // Handle resuming sync when returning to app.
        if (syncOnResume) {
            appSync.enableSyncPrecondition(AppSync.SyncPrecondition.HOME_SCREEN_REACHED)
        }

        UnregisterUserAccountActivityLauncher.launchIfAccountUnregistrationRequested(parentActivity = this)
    }

    override fun onStop() {
        super.onStop()
        appSync.removeSyncListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        appSync.disableSyncPrecondition(AppSync.SyncPrecondition.HOME_SCREEN_REACHED)

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
            // task may live longer, ensure notification is displayed again when resuming
            UnregisterUserAccountActivityLauncher.resetCooldown()
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
        backgroundOperationStatus.startOperation(BackgroundOperationStatus.Operation.SYNCHRONIZATION)
    }

    override fun onSyncCompleted() {
        backgroundOperationStatus.finishOperation(BackgroundOperationStatus.Operation.SYNCHRONIZATION)
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
    }
}
