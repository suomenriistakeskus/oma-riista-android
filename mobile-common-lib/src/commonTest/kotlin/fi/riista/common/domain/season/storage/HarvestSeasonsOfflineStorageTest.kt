package fi.riista.common.domain.season.storage

import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.dto.LocalizedStringDTO
import fi.riista.common.helpers.runBlockingTest
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDatePeriod
import fi.riista.common.model.LocalizedString
import fi.riista.common.model.toLocalDateDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HarvestSeasonsOfflineStorageTest {
    @Test
    fun `storing seasons saves them to repository`() = runBlockingTest {
        val storage = createOfflineStorage()

        val seasons = listOf(
            HarvestSeasonDTO(
                name = null,
                gameSpeciesCode = SpeciesCodes.COOT_ID,
                beginDate = LocalDate(2023, 1, 1).toLocalDateDTO(),
                endDate = LocalDate(2023, 1, 31).toLocalDateDTO(),
            )
        )

        assertNull(storage.repository.getHarvestSeasons(2022))

        storage.setHarvestSeasons(2022, seasons)

        assertEquals(seasons, storage.repository.getHarvestSeasons(2022))
    }

    @Test
    fun `storing seasons saves them to memory storage`() = runBlockingTest {
        val storage = createOfflineStorage()

        val seasons = listOf(
            HarvestSeasonDTO(
                name = LocalizedStringDTO("fi", "sv", "en"),
                gameSpeciesCode = SpeciesCodes.COOT_ID,
                beginDate = LocalDate(2023, 1, 1).toLocalDateDTO(),
                endDate = LocalDate(2023, 1, 31).toLocalDateDTO(),
            )
        )

        assertFalse(storage.memoryStorage.hasHarvestSeasons(2022))

        storage.setHarvestSeasons(2022, seasons)

        assertTrue(storage.memoryStorage.hasHarvestSeasons(2022))
        with (storage.memoryStorage.getHarvestSeasons(SpeciesCodes.COOT_ID, 2022)!!) {
            assertEquals(1, size)

            with (first()) {
                assertEquals(LocalizedString("fi", "sv", "en"), name)
                assertEquals(SpeciesCodes.COOT_ID, speciesCode)
                assertEquals(2022, huntingYear)
                assertEquals(
                    expected = listOf(
                        LocalDatePeriod(
                            beginDate = LocalDate(2023, 1, 1),
                            endDate = LocalDate(2023, 1, 31),
                        )
                    ),
                    actual = seasonPeriods
                )
            }
        }
    }

    @Test
    fun `memory storage is populated when seasons are queried`() = runBlockingTest {
        val storage = createOfflineStorage()

        val seasons = listOf(
            HarvestSeasonDTO(
                name = LocalizedStringDTO("fi", "sv", "en"),
                gameSpeciesCode = SpeciesCodes.COOT_ID,
                beginDate = LocalDate(2023, 1, 1).toLocalDateDTO(),
                endDate = LocalDate(2023, 1, 31).toLocalDateDTO(),
            )
        )

        assertFalse(storage.memoryStorage.hasHarvestSeasons(2022))

        storage.repository.saveHarvestSeasons(2022, seasons)

        assertFalse(storage.memoryStorage.hasHarvestSeasons(2022))

        val harvestSeasonsFromStorage = storage.getHarvestSeasons(SpeciesCodes.COOT_ID, 2022)
        assertNotNull(harvestSeasonsFromStorage)

        assertTrue(storage.memoryStorage.hasHarvestSeasons(2022))
        val harvestSeasonsFromMemoryStorage = storage.memoryStorage.getHarvestSeasons(SpeciesCodes.COOT_ID, 2022)
        assertEquals(harvestSeasonsFromStorage, harvestSeasonsFromMemoryStorage)

        with (harvestSeasonsFromStorage) {
            assertEquals(1, size)

            with (first()) {
                assertEquals(LocalizedString("fi", "sv", "en"), name)
                assertEquals(SpeciesCodes.COOT_ID, speciesCode)
                assertEquals(2022, huntingYear)
                assertEquals(
                    expected = listOf(
                        LocalDatePeriod(
                            beginDate = LocalDate(2023, 1, 1),
                            endDate = LocalDate(2023, 1, 31),
                        )
                    ),
                    actual = seasonPeriods
                )
            }
        }
    }

    private fun createOfflineStorage(): HarvestSeasonsOfflineStorage {
        return HarvestSeasonsOfflineStorage(
            repository = MockHarvestSeasonsRepository()
        )
    }
}