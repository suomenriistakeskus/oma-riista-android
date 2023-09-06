package fi.riista.common.network.sync

import kotlin.test.Test
import kotlin.test.assertEquals

class SyncDataPieceTest {
    @Test
    fun `data pieces included in SynchronizationLevel METADATA`() {
        assertEquals(
            expected = listOf(
                SyncDataPiece.SRVA_METADATA,
                SyncDataPiece.OBSERVATION_METADATA,
                SyncDataPiece.HARVEST_SEASONS,
                SyncDataPiece.METSAHALLITUS_PERMITS,
                SyncDataPiece.HUNTING_CLUB_OCCUPATIONS,
            ),
            actual = SyncDataPiece.getPiecesIncludedInSynchronizationLevel(SynchronizationLevel.METADATA)
        )
    }

    @Test
    fun `data pieces included in SynchronizationLevel USER_CONTENT`() {
        assertEquals(
            expected = listOf(
                SyncDataPiece.HUNTING_CONTROL,
                SyncDataPiece.SRVA_METADATA,
                SyncDataPiece.OBSERVATION_METADATA,
                SyncDataPiece.HARVEST_SEASONS,
                SyncDataPiece.METSAHALLITUS_PERMITS,
                SyncDataPiece.HUNTING_CLUB_OCCUPATIONS,
                SyncDataPiece.SRVA_EVENTS,
                SyncDataPiece.OBSERVATIONS,
                SyncDataPiece.HARVESTS,
            ),
            actual = SyncDataPiece.getPiecesIncludedInSynchronizationLevel(SynchronizationLevel.USER_CONTENT)
        )
    }
}
