package fi.riista.mobile.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie
import fi.riista.common.RiistaSDK
import fi.riista.common.network.cookies.CookieData
import fi.riista.mobile.RiistaApplication
import fi.riista.mobile.feature.login.LoginActivity
import fi.riista.mobile.network.RegisterPushDeviceTask
import fi.riista.mobile.observation.ObservationMetadataHelper
import fi.riista.mobile.srva.SrvaParametersHelper
import fi.vincit.androidutilslib.context.WorkContext
import java.util.*

private const val TAG = "AuthenticatorImpl"

class AuthenticatorImpl(
    private val appWorkContext: WorkContext,
    private val credentialsStore: CredentialsStore,
    private val userInfoStore: UserInfoStore,
) : Authenticator {

    override suspend fun authenticate(
        username: String,
        password: String,
        timeoutSeconds: Int,
        callback: Authenticator.AuthCallback?,
    ) {
        val loginResponse = RiistaSDK.login(username, password, timeoutSeconds)

        loginResponse.onSuccess { _, data ->
            loginSucceed()

            copyAuthenticationCookiesFromRiistaSDK()

            credentialsStore.save(username, password)
            handleUserInfoResult(data.raw)
            callback?.onLoginSuccessful(data.raw)

            loginSync()
        }
        loginResponse.onError { statusCode, exception ->
            loginFailed(statusCode, exception?.message)
            callback?.onLoginFailed(statusCode ?: 0)
        }
        loginResponse.onCancel {
            // probably timeout
            loginCancelled()
            callback?.onLoginFailed(httpStatusCode = 0)
        }
    }

    /**
     * Copies authentication cookies from Riista SDK so that AndroidUtilsLib networking
     * can also perform authenticated requests to backend.
     */
    private fun copyAuthenticationCookiesFromRiistaSDK() {
        val riistaSdkCookies: List<CookieData> = RiistaSDK.getAllNetworkCookies()

        val cookieStore = CookieStoreSingleton.INSTANCE.cookieStore
        riistaSdkCookies.forEach { cookieData ->
            val cookie = BasicClientCookie(cookieData.name, cookieData.value)
            cookieData.domain?.let { cookie.domain = it }
            cookieData.path?.let { cookie.path = it }
            cookieData.expiresTimestamp?.let { cookie.expiryDate = Date(it) }
            cookie.isSecure = cookieData.secure

            cookieStore.addCookie(cookie)
        }
    }

    override suspend fun reauthenticate(callback: Authenticator.AuthCallback?, timeoutSeconds: Int) {
        credentialsStore.get()?.let { cred ->
            val loginResponse = RiistaSDK.login(cred.username, cred.password, timeoutSeconds)

            loginResponse.onSuccess { _, data ->
                loginSucceed()

                copyAuthenticationCookiesFromRiistaSDK()

                handleUserInfoResult(data.raw)
                callback?.onLoginSuccessful(data.raw)

                loginSync()
            }
            loginResponse.onError { statusCode, exception ->
                loginFailed(statusCode, exception?.message)

                callback?.onLoginFailed(statusCode ?: 0)

                if (statusCode == 401 || statusCode == 403) {
                    credentialsStore.clear()

                    val context: Context = appWorkContext.context
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
            loginResponse.onCancel {
                // probably timeout
                loginCancelled()
                callback?.onLoginFailed(httpStatusCode = 0)
            }
        }
    }


    private fun handleUserInfoResult(userInfoAsJson: String?) {
        userInfoStore.setUserInfo(userInfoAsJson)
    }

    private fun loginSync() {
        // When login attempt ends, try to fetch observation metadata and SRVA parameters.
        // TODO Move fetching data to AppSync.
        ObservationMetadataHelper.instance.fetchMetadata()
        SrvaParametersHelper.instance.fetchParameters()

        sendFcmRegistrationToServer()
    }

    private fun sendFcmRegistrationToServer() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val instanceToken = task.result
            Log.d(TAG, "Current registration id is: $instanceToken")
            val workContext = RiistaApplication.getInstance().workContext
            val registerPushDeviceTask = RegisterPushDeviceTask(workContext, instanceToken)
            registerPushDeviceTask.start()
        }
    }

    // TODO: Remove these when Riista SDK login has proven to be working
    private class LoginSuccess : RuntimeException()
    private class LoginFailure : RuntimeException()
    private class LoginCancelled : RuntimeException()

    private fun loginFailed(statusCode: Int?, message: String?) {
        FirebaseCrashlytics.getInstance().setCustomKey("failure_reason", "${statusCode ?: -1} $message")
        FirebaseCrashlytics.getInstance().recordException(LoginFailure())
    }

    private fun loginCancelled() {
        FirebaseCrashlytics.getInstance().recordException(LoginCancelled())
    }

    private fun loginSucceed() {
        FirebaseCrashlytics.getInstance().recordException(LoginSuccess())
    }
}
