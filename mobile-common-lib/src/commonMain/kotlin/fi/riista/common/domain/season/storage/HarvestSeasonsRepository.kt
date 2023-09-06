package fi.riista.common.domain.season.storage

import fi.riista.common.database.DatabaseWriteContext
import fi.riista.common.database.DatabaseDriverFactory
import fi.riista.common.database.RiistaDatabase
import fi.riista.common.domain.model.HuntingYear
import fi.riista.common.domain.season.dto.HarvestSeasonDTO
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson
import kotlinx.coroutines.withContext

interface HarvestSeasonsRepository {
    suspend fun saveHarvestSeasons(huntingYear: HuntingYear, harvestSeasonsDTOs: List<HarvestSeasonDTO>)
    fun getHarvestSeasons(huntingYear: HuntingYear): List<HarvestSeasonDTO>?
}

internal class HarvestSeasonsDatabaseRepository(
    databaseDriverFactory: DatabaseDriverFactory
): HarvestSeasonsRepository {
    private val database = RiistaDatabase(
        driver = databaseDriverFactory.createDriver(),
    )

    private val harvestSeasonsQueries = database.dbHarvestSeasonsQueries

    override suspend fun saveHarvestSeasons(huntingYear: HuntingYear,harvestSeasonsDTOs: List<HarvestSeasonDTO>) {
        saveHarvestSeasonDAOs(
            huntingYear = huntingYear,
            harvestSeasonsDAOs = harvestSeasonsDTOs.map { it.toHarvestSeasonDAO() },
        )
    }

    override fun getHarvestSeasons(huntingYear: HuntingYear): List<HarvestSeasonDTO>? {
        return getHarvestSeasonDAOs(huntingYear)?.map { it.toHarvestSeasonDTO() }
    }

    private suspend fun saveHarvestSeasonDAOs(
        huntingYear: HuntingYear,
        harvestSeasonsDAOs: List<HarvestSeasonDAO>,
    ) = withContext(DatabaseWriteContext) {
        harvestSeasonsQueries.transaction {
            val existingHarvestSeasons = harvestSeasonsQueries.selectHarvestSeason(
                hunting_year = huntingYear,
                json_format_version = HarvestSeasonDAO.DAO_VERSION
            ).executeAsOneOrNull()

            val harvestSeasonsJson = harvestSeasonsDAOs.serializeToJson()
            if (existingHarvestSeasons == null) {
                harvestSeasonsQueries.insertHarvestSeasons(
                    hunting_year = huntingYear,
                    json_format_version = HarvestSeasonDAO.DAO_VERSION,
                    harvest_seasons_json = harvestSeasonsJson
                )
            } else {
                harvestSeasonsQueries.updateHarvestSeasons(
                    hunting_year = huntingYear,
                    json_format_version = HarvestSeasonDAO.DAO_VERSION,
                    harvest_seasons_json = harvestSeasonsJson
                )
            }
        }
    }

    private fun getHarvestSeasonDAOs(huntingYear: HuntingYear): List<HarvestSeasonDAO>? {
        val existingHarvestSeasonsJson = harvestSeasonsQueries.selectHarvestSeason(
            hunting_year = huntingYear,
            json_format_version = HarvestSeasonDAO.DAO_VERSION
        ).executeAsOneOrNull()?.harvest_seasons_json

        return existingHarvestSeasonsJson?.deserializeFromJson()
    }
}
