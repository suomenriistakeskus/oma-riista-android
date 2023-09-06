package fi.riista.mobile.feature.unregister

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import dagger.android.AndroidInjection
import fi.riista.common.RiistaSDK
import fi.riista.common.model.LocalDateTime
import fi.riista.mobile.R
import fi.riista.mobile.activity.BaseActivity
import fi.riista.mobile.riistaSdkHelpers.fromJodaDateTime
import fi.riista.mobile.riistaSdkHelpers.toJodaDateTime
import fi.riista.mobile.ui.AlertDialogFragment
import fi.riista.mobile.ui.AlertDialogId
import fi.riista.mobile.ui.BusyIndicatorView
import fi.riista.mobile.utils.LogoutHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.joda.time.DateTime
import javax.inject.Inject

class UnregisterUserAccountActivity : BaseActivity(),
    AccountUnregistrationRequestedFragment.Listener,
    AskAccountUnregistrationFragment.Listener {

    private lateinit var busyIndicator: BusyIndicatorView

    @Inject
    lateinit var logoutHelper: LogoutHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unregister_user_account)

        busyIndicator = findViewById(R.id.view_busy_indicator)

        val enableNotifyButtons = getEnableNotifyButtons(intent?.extras)
        val unregistrationRequestDatetime = getUnregistrationRequestedDateTime(intent?.extras)
        val (fragment, fragmentTag) = when (unregistrationRequestDatetime) {
            null -> AskAccountUnregistrationFragment() to AskAccountUnregistrationFragment.TAG
            else -> AccountUnregistrationRequestedFragment.create(
                unregistrationRequestedDatetime = unregistrationRequestDatetime,
                enableContinueUsingService = enableNotifyButtons,
                enableCancel = enableNotifyButtons,
            ) to AccountUnregistrationRequestedFragment.TAG
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment, fragmentTag)
            .commit()
    }

    override fun onContinueServiceClicked() {
        MainScope().launch {
            busyIndicator.show()

            val success = RiistaSDK.cancelUnregisterAccount()

            yield()

            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED).not()) {
                return@launch
            }

            busyIndicator.hide {
                if (success) {
                    finish()
                } else {
                    AlertDialogFragment.Builder(this@UnregisterUserAccountActivity, AlertDialogId.UNREGISTER_OPERATION_FAILED)
                        .setMessage(R.string.group_hunting_operation_failed)
                        .setPositiveButton(R.string.ok)
                        .build()
                        .show(supportFragmentManager)
                }
            }
        }
    }

    override fun onRequestAccountUnregistration() {
        MainScope().launch {
            busyIndicator.show()

            val unregistrationRequestDatetime = RiistaSDK.unregisterAccount()

            yield()

            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED).not()) {
                return@launch
            }

            busyIndicator.hide {
                if (unregistrationRequestDatetime != null) {
                    logoutAndNotifyAboutAccountUnregistration(
                        unregistrationRequestDatetime = unregistrationRequestDatetime
                    )
                } else {
                    AlertDialogFragment.Builder(this@UnregisterUserAccountActivity, AlertDialogId.UNREGISTER_OPERATION_FAILED)
                        .setMessage(R.string.group_hunting_operation_failed)
                        .setPositiveButton(R.string.ok)
                        .build()
                        .show(supportFragmentManager)
                }
            }
        }
    }

    override fun onCancelClicked() {
        finish()
    }

    private fun logoutAndNotifyAboutAccountUnregistration(unregistrationRequestDatetime: LocalDateTime) {
        val activity = this
        MainScope().launch {
            logoutHelper.logout(context = activity)

            // launch this same activity with different parameters instead of displaying the fragment
            // -> this allows us the clear all other activities so that user is unable to navigate back
            val launchIntent = getLaunchIntent(
                context = activity,
                unregistrationRequestDatetime = unregistrationRequestDatetime,
                enableNotifyButtons = false
            )

            // remove previous activities + ensure new activity is the only one in task
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            activity.startActivity(launchIntent)
        }
    }

    companion object {

        fun getLaunchIntent(context: Context): Intent {
            val unregistrationRequestDatetime = RiistaSDK.accountService.userAccountUnregistrationRequestDatetime

            return getLaunchIntent(context, unregistrationRequestDatetime, enableNotifyButtons = true)
        }

        private fun getLaunchIntent(
            context: Context,
            unregistrationRequestDatetime: LocalDateTime?,
            enableNotifyButtons: Boolean,
        ): Intent {
            return Intent(context, UnregisterUserAccountActivity::class.java).apply {
                val extras = Bundle().apply {
                    unregistrationRequestDatetime?.let {
                        putSerializable(KEY_UNREGISTRATION_REQUEST_DATE_TIME, it.toJodaDateTime())
                    }
                    putBoolean(KEY_ENABLE_NOTIFY_BUTTONS, enableNotifyButtons)
                }

                putExtras(extras)
            }
        }

        private fun getUnregistrationRequestedDateTime(extras: Bundle?): LocalDateTime? {
            return extras?.getSerializable(KEY_UNREGISTRATION_REQUEST_DATE_TIME)?.let { serializable ->
                    (serializable as? DateTime)
                }?.let { datetime ->
                    LocalDateTime.fromJodaDateTime(datetime)
                }
        }

        private fun getEnableNotifyButtons(extras: Bundle?) =
            extras?.getBoolean(KEY_ENABLE_NOTIFY_BUTTONS,true) ?: true

        private const val KEY_BASE = "UnregisterUserAccountActivity"
        private const val KEY_UNREGISTRATION_REQUEST_DATE_TIME = "${KEY_BASE}_UNREGISTRATION_REQUEST_DATE_TIME"
        private const val KEY_ENABLE_NOTIFY_BUTTONS = "${KEY_BASE}_ENABLE_NOTIFY_BUTTONS"
    }
}

object UnregisterUserAccountActivityLauncher {
    private const val LAUNCH_COOLDOWN_MINUTES = 60

    private var lastLaunchTime: DateTime? = null

    fun launchIfAccountUnregistrationRequested(parentActivity: Activity) {
        if (RiistaSDK.accountService.userAccountUnregistrationRequestDatetime == null) {
            return
        }

        val now = DateTime.now()
        lastLaunchTime?.let {
            if (it.plusMinutes(LAUNCH_COOLDOWN_MINUTES) > now) {
                return
            }
        }
        lastLaunchTime = now

        parentActivity.startActivity(UnregisterUserAccountActivity.getLaunchIntent(context = parentActivity))
    }

    /**
     * Resets the cooldown which allows the next attempt to succeed.
     */
    fun resetCooldown() {
        lastLaunchTime = null
    }
}
