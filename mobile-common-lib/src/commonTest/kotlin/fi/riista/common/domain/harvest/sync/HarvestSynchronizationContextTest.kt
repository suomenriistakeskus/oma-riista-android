package fi.riista.common.domain.harvest.sync

import fi.riista.common.RiistaSDK
import fi.riista.common.domain.constants.Constants
import fi.riista.common.helpers.initializeMocked
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDateTime
import fi.riista.common.network.BackendAPIMock
import fi.riista.common.network.sync.SyncDataPiece
import fi.riista.common.network.sync.SynchronizationConfig
import fi.riista.common.preferences.MockPreferences
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HarvestSynchronizationContextTest {
    @Test
    fun `all harvests are re-fetched if spec version is changed`() = runBlockingTest {
        val backendAPIMock = BackendAPIMock()
        val preferences = MockPreferences()
        RiistaSDK.initializeMocked(
            mockBackendAPI = backendAPIMock,
            mockPreferences = preferences,
            performLoginWithPentti = true,
        )

        assertEquals(0, backendAPIMock.callCount(BackendAPIMock::fetchHarvests), "before sync")
        assertNull(preferences.getInt(HarvestSynchronizationContext.KEY_HARVEST_SYNC_SPEC_VERSION))

        RiistaSDK.synchronize(
            syncDataPiece = SyncDataPiece.HARVESTS,
            config = SynchronizationConfig.DEFAULT
        )

        // initial sync, no datetime should be used
        assertEquals(1, backendAPIMock.callCount(BackendAPIMock::fetchHarvests), "sync 1")
        assertEquals(
            expected = null,
            actual = backendAPIMock.callParameter(BackendAPIMock::fetchHarvests),
            message = "timestamp / sync 1"
        )
        assertEquals(
            expected = Constants.HARVEST_SPEC_VERSION,
            actual = preferences.getInt(HarvestSynchronizationContext.KEY_HARVEST_SYNC_SPEC_VERSION)
        )

        delay(100)
        RiistaSDK.synchronize(
            syncDataPiece = SyncDataPiece.HARVESTS,
            config = SynchronizationConfig.DEFAULT
        )

        // from MockHarvestPageData.harvestPageWithOneHarvest
        val lastContentTimeStamp = LocalDateTime(2023, 1, 20, 12, 52, 45)
        assertEquals(2, backendAPIMock.callCount(BackendAPIMock::fetchHarvests), "sync 2")
        assertEquals(
            expected = lastContentTimeStamp,
            actual = backendAPIMock.callParameter(BackendAPIMock::fetchHarvests),
            message = "timestamp / sync 2"
        )


        delay(100)
        preferences.putInt(HarvestSynchronizationContext.KEY_HARVEST_SYNC_SPEC_VERSION, Constants.HARVEST_SPEC_VERSION - 1)

        RiistaSDK.synchronize(
            syncDataPiece = SyncDataPiece.HARVESTS,
            config = SynchronizationConfig.DEFAULT
        )

        assertEquals(3, backendAPIMock.callCount(BackendAPIMock::fetchHarvests), "sync 3")
        assertEquals(
            expected = null,
            actual = backendAPIMock.callParameter(BackendAPIMock::fetchHarvests),
            message = "timestamp / sync 3"
        )
    }
}
