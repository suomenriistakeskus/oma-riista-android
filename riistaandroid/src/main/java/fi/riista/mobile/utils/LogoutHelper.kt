package fi.riista.mobile.utils

import android.content.Context
import fi.riista.common.RiistaSDK
import fi.riista.mobile.database.HarvestDatabase
import fi.riista.mobile.database.PermitManager
import fi.riista.mobile.feature.unregister.UnregisterUserAccountActivityLauncher
import fi.riista.mobile.sync.AppSync
import fi.riista.mobile.sync.AppSyncPrecondition

class LogoutHelper constructor(
    private val appSync: AppSync,
    private val harvestDatabase: HarvestDatabase,
    private val credentialsStore: CredentialsStore,
    private val permitManager: PermitManager,
) {
    suspend fun logout(context: Context) {
        RiistaSDK.logout()

        // also stops periodic metadata sync
        appSync.disableSyncPrecondition(AppSyncPrecondition.CREDENTIALS_VERIFIED)

        harvestDatabase.clearUpdateTimes()
        credentialsStore.clear()
        AppPreferences.clearAll(context)
        permitManager.clearPermits()
        Utils.unregisterNotificationsAsync()

        UnregisterUserAccountActivityLauncher.resetCooldown()
    }
}
