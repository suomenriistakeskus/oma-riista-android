package fi.riista.mobile.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.android.AndroidInjection
import fi.riista.mobile.R
import fi.riista.mobile.RiistaApplication
import fi.riista.mobile.models.announcement.Announcement
import fi.riista.mobile.network.CheckVersionTask
import fi.riista.mobile.storage.StorageDatabase
import fi.riista.mobile.ui.UpdateAvailableDialog
import fi.riista.mobile.utils.*
import fi.riista.mobile.utils.Authenticator.AuthCallback
import fi.riista.mobile.utils.Authenticator.AuthSuccessCallback
import fi.riista.mobile.utils.LocaleUtil.localeFromLanguageSetting
import fi.vincit.androidutilslib.activity.WorkActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

/**
 * Login screen is skipped if user has stored credentials
 */
class LoginActivity : WorkActivity() {
    @Inject
    lateinit var authenticator: Authenticator

    @Inject
    lateinit var credentialsStore: CredentialsStore

    private lateinit var mUsernameInput: TextInputEditText
    private lateinit var mPasswordInput: TextInputEditText
    private lateinit var mLoginButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setupLocaleFromSetting()
        setContentView(R.layout.activity_loginscreen)
        mUsernameInput = findViewById(R.id.username)
        mPasswordInput = findViewById(R.id.password)
        mLoginButton = findViewById(R.id.loginbutton)

        val activityLaunchedByAnnouncementNotification = extractAnnouncementFromNotificationIntentData()
        setupButtons(activityLaunchedByAnnouncementNotification)
    }

    override fun onResume() {
        super.onResume()
        mLoginButton.isEnabled = Utils.isPlayServicesAvailable(this, true)
        if (!RiistaApplication.sDidCheckVersion) {
            RiistaApplication.sDidCheckVersion = true
            checkVersionUpdate()
        }
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
            authenticator.authenticate(username, password, LoginCallback(this@LoginActivity))
        }
        mLoginButton.isEnabled = false
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
                    authenticator.reauthenticate(
                            ReloginCallback(
                                    this@LoginActivity,
                                    isActivityStartedByAnnouncementNotification
                            )
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
            mLoginButton.isEnabled = true
            mLoginButton.setOnClickListener {
                val username = findViewById<EditText>(R.id.username).text.toString()
                val password = findViewById<EditText>(R.id.password).text.toString()
                attemptLogin(username, password)
            }
        }
        findViewById<View>(R.id.layout).setOnTouchListener { view: View, ev: MotionEvent ->
            if (ev.action == MotionEvent.ACTION_UP) {
                view.performClick()
            }
            if (mUsernameInput.isFocused) {
                mUsernameInput.clearFocus()
            }
            if (mPasswordInput.isFocused) {
                mPasswordInput.clearFocus()
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
            mActivityReference.get()?.takeIf { !it.isFinishing }?.onLoginFailed(httpStatusCode)
        }

    }

    private fun onLoginSuccessful() {
        val intent = Intent(this, MainActivity::class.java)
        replaceActivity(intent)
    }

    private fun onLoginFailed(httpStatusCode: Int) {
        @StringRes val msgResId: Int = when (httpStatusCode) {
            418 -> {
                R.string.version_outdated
            }
            403 -> {
                R.string.login_failed
            }
            else -> {
                R.string.connecting_failed
            }
        }
        val message = getString(msgResId)
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int -> }
            .show()

        mLoginButton.isEnabled = true
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
        isActivityStartedByAnnouncementNotification: Boolean
    ) : AuthSuccessCallback() {
        private val mActivityReference: WeakReference<LoginActivity> = WeakReference(activity)
        private val mIsActivityStartedByAnnouncementNotification: Boolean =
            isActivityStartedByAnnouncementNotification

        override fun onLoginSuccessful(userInfo: String?) {
            mActivityReference.get()
                ?.takeIf { mIsActivityStartedByAnnouncementNotification }
                ?.onReloginSuccessful()
        }

    }

    companion object {
        const val SHOW_ANNOUNCEMENTS_EXTRA = "showAnnouncements"
        private const val TAG = "LoginActivity"
    }
}
