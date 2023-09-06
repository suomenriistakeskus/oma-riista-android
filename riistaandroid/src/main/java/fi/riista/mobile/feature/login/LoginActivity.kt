package fi.riista.mobile.feature.login

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dagger.android.AndroidInjection
import fi.riista.mobile.R
import fi.riista.mobile.RiistaApplication
import fi.riista.mobile.activity.MainActivity
import fi.riista.mobile.models.announcement.Announcement
import fi.riista.mobile.network.CheckVersionTask
import fi.riista.mobile.storage.StorageDatabase
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.sync.AppSyncPrecondition
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.UpdateAvailableDialog
import fi.riista.mobile.utils.Authenticator
import fi.riista.mobile.utils.Authenticator.AuthCallback
import fi.riista.mobile.utils.BackgroundOperationStatus
import fi.riista.mobile.utils.CredentialsStore
import fi.riista.mobile.utils.JsonUtils
import fi.riista.mobile.utils.KeyboardUtils
import fi.riista.mobile.utils.LocaleUtil.localeFromLanguageSetting
import fi.riista.mobile.utils.Utils
import fi.riista.mobile.utils.isResumed
import fi.vincit.androidutilslib.activity.WorkActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

interface LoginAttemptFailedListener {
    fun loginFailed()
}

/**
 * Login screen is skipped if user has stored credentials
 */
class LoginActivity
    : WorkActivity()
    , LoginFragment.Manager
    , ResetPasswordFragment.Manager
    , EmailChangedFragment.Manager
{

    @Inject
    lateinit var authenticator: Authenticator

    @Inject
    lateinit var appSync: AppSync

    @Inject
    lateinit var credentialsStore: CredentialsStore

    @Inject
    lateinit var backgroundOperationStatus: BackgroundOperationStatus

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: ScreenSlidePagerAdapter

    private var loginAttemptListener: LoginAttemptFailedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setupLocaleFromSetting()
        setContentView(R.layout.activity_loginscreen)

        viewPager = findViewById(R.id.view_pager)
        pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.isUserInputEnabled = false // Disable swipe

        val activityLaunchedByAnnouncementNotification = extractAnnouncementFromNotificationIntentData()
        setupButtons(activityLaunchedByAnnouncementNotification)
    }

    override fun onResume() {
        super.onResume()
        if (!RiistaApplication.sDidCheckVersion) {
            RiistaApplication.sDidCheckVersion = true
            checkVersionUpdate()
        }
    }

    override fun startLogin(username: String, password: String, listener: LoginAttemptFailedListener) {
        loginAttemptListener = listener
        attemptLogin(username, password)
    }

    override fun startResetPassword() {
        pagerAdapter.tab2State = ScreenSlidePagerAdapter.Tab2State.RESET_PASSWORD
        viewPager.currentItem = 1
    }

    override fun startEmailChanged() {
        pagerAdapter.tab2State = ScreenSlidePagerAdapter.Tab2State.EMAIL_CHANGED
        viewPager.currentItem = 1
    }

    override fun startRegistration() {
        pagerAdapter.tab1State = ScreenSlidePagerAdapter.Tab1State.CREATE_ACCOUNT
        viewPager.currentItem = 0
    }

    override fun returnToLogin() {
        pagerAdapter.tab1State = ScreenSlidePagerAdapter.Tab1State.LOGIN
        viewPager.currentItem = 0
    }

    // This method is relevant in case a message with both notification and data payload is
    // received while application is not in the foreground (either not active or not running at
    // all). The notification is then delivered to the device’s system tray, and the data payload
    // is delivered in the extras of the intent of your launcher Activity.
    //
    // Returns true if announcement was present in notification intent.
    private fun extractAnnouncementFromNotificationIntentData(): Boolean {
        val announcement = intent.getStringExtra("announcement") ?: return false
        JsonUtils.jsonToObject(announcement, Announcement::class.java, true)
            ?.let {
                // We got announcement data from a notification, so save it if we can.
                StorageDatabase.getInstance().updateAnnouncement(it, null)
            }

        return true
    }

    private fun attemptLogin(username: String, password: String) {
        CoroutineScope(Main).launch {
            authenticator.authenticate(
                username = username,
                password = password,
                timeoutSeconds = Authenticator.DEFAULT_AUTHENTICATION_TIMEOUT_SECONDS,
                callback = LoginCallback(this@LoginActivity)
            )
        }
    }

    private fun setupLocaleFromSetting() {
        val locale = localeFromLanguageSetting(this)
        Locale.setDefault(locale)
        val config = Configuration()
        // TODO: 2.2.2021 fix deprecated locale usage
        config.locale = locale
        val res = baseContext.resources
        res.updateConfiguration(config, res.displayMetrics)
    }

    private fun setupButtons(isActivityStartedByAnnouncementNotification: Boolean) {
        if (Utils.isPlayServicesAvailable(this, true)) {

            // Login automatically if credentials are stored.
            if (credentialsStore.isCredentialsSaved()) {
                CoroutineScope(Main).launch {
                    val indicateReloginOperation = !isActivityStartedByAnnouncementNotification
                    if (indicateReloginOperation) {
                        // indicate relogin if not starting from announcement
                        backgroundOperationStatus.startOperation(BackgroundOperationStatus.Operation.INITIAL_RELOGIN)
                    }

                    authenticator.reauthenticate(
                        callback = ReloginCallback(
                            activity = this@LoginActivity,
                            appSync = appSync,
                            backgroundOperationStatus = backgroundOperationStatus.takeIf { indicateReloginOperation },
                            isActivityStartedByAnnouncementNotification = isActivityStartedByAnnouncementNotification
                        ),
                        // let first attempt be fast one i.e. timeout early if we don't have response soon enough
                        // -> this approach enables e.g. manual sync possibility faster if it seems that response
                        //    is taking longer to receive
                        timeoutSeconds = 20
                    )
                }
                val intent = Intent(this, MainActivity::class.java)

                // Sync is not enabled now since MainActivity will be triggered a second time
                // via onNewIntent method after login task has completed.
                intent.putExtra(
                    MainActivity.DO_NOT_INITIATE_SYNC,
                    isActivityStartedByAnnouncementNotification
                )

                startActivity(intent)
                if (!isActivityStartedByAnnouncementNotification) {
                    // In case of announcement notification finish() will be called when login
                    // task has completed.
                    finish()
                }
                return
            }
        }
        findViewById<View>(R.id.layout).setOnTouchListener { view: View, ev: MotionEvent ->
            if (ev.action == MotionEvent.ACTION_UP) {
                view.performClick()
            }
            KeyboardUtils.hideKeyboard(this@LoginActivity, view)
            false
        }
    }

    private fun replaceActivity(intent: Intent) {
        startActivity(intent)
        finish()
    }

    private fun checkVersionUpdate() {
        Log.d(TAG, "Check version")
        val task: CheckVersionTask = object : CheckVersionTask(workContext) {
            override fun onFinishText(text: String) {
                Log.d(TAG, "Version check finished $text")
                RiistaApplication.sDidCheckVersion = true
                if (Utils.shouldDisplayVersionUpdateDialog(text)) {
                    UpdateAvailableDialog.show(this@LoginActivity, "", 0)
                }
            }

            override fun onError() {
                Log.d(TAG, "Version check error")
            }
        }
        task.start()
    }

    private class LoginCallback(activity: LoginActivity) : AuthCallback {
        private val mActivityReference: WeakReference<LoginActivity> = WeakReference(activity)

        override fun onLoginSuccessful(userInfo: String?) {
            mActivityReference.get()?.onLoginSuccessful()
        }

        override fun onLoginFailed(httpStatusCode: Int) {
            mActivityReference.get()?.takeIf { it.lifecycle.isResumed() }?.onLoginFailed(httpStatusCode)
        }
    }

    private fun onLoginSuccessful() {
        appSync.enableSyncPrecondition(AppSyncPrecondition.CREDENTIALS_VERIFIED)

        val intent = Intent(this, MainActivity::class.java)
        replaceActivity(intent)
    }

    private fun onLoginFailed(httpStatusCode: Int) {
        loginAttemptListener?.loginFailed()

        @StringRes val msgResId: Int = when (httpStatusCode) {
            418 -> {
                R.string.version_outdated
            }
            403 -> {
                appSync.disableSyncPrecondition(AppSyncPrecondition.CREDENTIALS_VERIFIED)
                R.string.login_failed
            }
            else -> {
                R.string.login_connect_failed
            }
        }
        AlertDialogFragment.Builder(this, AlertDialogId.LOGIN_ACTIVITY_LOGIN_FAILED)
            .setMessage(msgResId)
            .setPositiveButton(R.string.ok)
            .build()
            .show(supportFragmentManager)
    }

    private fun onReloginSuccessful() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(SHOW_ANNOUNCEMENTS_EXTRA, true)

        // Instead of creating a new instance of MainActivity on top of current one,
        // onNewIntent method of the current instance will be called.
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        replaceActivity(intent)
    }

    private class ReloginCallback(
        activity: LoginActivity,
        private val appSync: AppSync,
        private val backgroundOperationStatus: BackgroundOperationStatus?,
        isActivityStartedByAnnouncementNotification: Boolean
    ) : AuthCallback {
        private val mActivityReference: WeakReference<LoginActivity> = WeakReference(activity)
        private val mIsActivityStartedByAnnouncementNotification: Boolean =
            isActivityStartedByAnnouncementNotification

        override fun onLoginSuccessful(userInfo: String?) {
            updateCredentialsPrecondition(
                credentialsShouldBeValid = true
            )

            backgroundOperationStatus?.finishOperation(BackgroundOperationStatus.Operation.INITIAL_RELOGIN)

            mActivityReference.get()
                ?.takeIf { mIsActivityStartedByAnnouncementNotification }
                ?.onReloginSuccessful()
        }

        override fun onLoginFailed(httpStatusCode: Int) {
            backgroundOperationStatus?.finishOperation(BackgroundOperationStatus.Operation.INITIAL_RELOGIN)

            updateCredentialsPrecondition(
                credentialsShouldBeValid = httpStatusCode != 401 && httpStatusCode != 403
            )
        }

        private fun updateCredentialsPrecondition(credentialsShouldBeValid: Boolean) {
            if (credentialsShouldBeValid) {
                // other failures shouldn't prevent appsync attempts
                appSync.enableSyncPrecondition(AppSyncPrecondition.CREDENTIALS_VERIFIED)
            } else {
                // only clear logged in status if credentials were not ok
                appSync.disableSyncPrecondition(AppSyncPrecondition.CREDENTIALS_VERIFIED)
            }
        }
    }

    companion object {
        const val SHOW_ANNOUNCEMENTS_EXTRA = "showAnnouncements"
        private const val TAG = "LoginActivity"
    }

    private class ScreenSlidePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        enum class Tab1State {
            LOGIN,
            CREATE_ACCOUNT,
        }
        enum class Tab2State {
            EMAIL_CHANGED,
            RESET_PASSWORD,
        }

        var tab1State: Tab1State = Tab1State.LOGIN
            set(value) {
                field = value
                notifyItemChanged(0)
            }
        var tab2State: Tab2State = Tab2State.EMAIL_CHANGED
            set(value) {
                field = value
                notifyItemChanged(1)
            }

        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    when (tab1State) {
                        Tab1State.LOGIN -> LoginFragment.create(LoginFragment.Tab.LOGIN_TAB)
                        Tab1State.CREATE_ACCOUNT -> LoginFragment.create(LoginFragment.Tab.CREATE_ACCOUNT_TAB)
                    }
                }
                1 -> {
                    when (tab2State) {
                        Tab2State.EMAIL_CHANGED -> EmailChangedFragment()
                        Tab2State.RESET_PASSWORD -> ResetPasswordFragment()
                    }
                }
                else -> throw IllegalStateException("Invalid position in viewPager $position")
            }
        }
    }
}
