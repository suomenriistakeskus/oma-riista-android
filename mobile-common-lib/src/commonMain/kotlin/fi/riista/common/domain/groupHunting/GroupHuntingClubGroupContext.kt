package fi.riista.common.domain.groupHunting

import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.validation.isValid
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.model.LocalDate
import fi.riista.common.network.BackendApiProvider
import fi.riista.common.util.LocalDateTimeProvider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

enum class HuntingClubGroupDataPiece {
    MEMBERS,
    HUNTING_AREA,
    STATUS,
    HUNTING_DAYS,
    DIARY,
}


/**
 * A group hunting context for a single club group (e.g. Hirviporukka 2021).
 */
@Suppress("MemberVisibilityCanBePrivate")
class GroupHuntingClubGroupContext internal constructor(
    backendApiProvider: BackendApiProvider,
    val huntingGroup: HuntingGroup,
): BackendApiProvider by backendApiProvider {

    val membersProvider: HuntingGroupMembersProvider = HuntingGroupMembersFromNetworkProvider(
            backendApiProvider, huntingGroup.id)

    val huntingAreaProvider: HuntingGroupAreaProvider = HuntingGroupAreaFromNetworkProvider(
            backendApiProvider, huntingGroup.id)

    val huntingStatusProvider: HuntingGroupStatusProvider = HuntingGroupStatusFromNetworkProvider(
            backendApiProvider, huntingGroup.id)

    private val _huntingDaysProvider = GroupHuntingDayFromNetworkProvider(
            backendApiProvider, huntingGroup.id)
    val huntingDaysProvider: GroupHuntingDayProvider = _huntingDaysProvider

    val huntingDayUpdater: GroupHuntingDayUpdater = GroupHuntingDayNetworkUpdater(
            backendApiProvider,
    )

    val diaryProvider: GroupHuntingDiaryProvider = GroupHuntingDiaryNetworkProvider(
            backendApiProvider, huntingGroup.id)

    internal val harvestUpdater: GroupHuntingHarvestUpdater = GroupHuntingHarvestNetworkUpdater(
        backendApiProvider, huntingGroup.id
    )

    val observationUpdater: GroupHuntingObservationUpdater = GroupHuntingObservationNetworkUpdater(
        backendApiProvider, huntingGroup.id
    )

    val target: HuntingGroupTarget
        get() = huntingGroup.getTarget()

    suspend fun fetchAllData(refresh: Boolean) = coroutineScope {
        fetchDataPieces(HuntingClubGroupDataPiece.values().toList(), refresh)
    }

    suspend fun fetchDataPieces(dataPieces: List<HuntingClubGroupDataPiece>, refresh: Boolean) = coroutineScope {
        val fetchJobs = dataPieces.map { dataPiece ->
            launch {
                fetchData(dataPiece, refresh)
            }
        }
        fetchJobs.joinAll()
    }

    /**
     * Attempts to find a [GroupHuntingHarvest] that has the same id as specified by
     * given [identifiesHarvest]
     */
    fun findHarvest(identifiesHarvest: IdentifiesGroupHuntingHarvest): GroupHuntingHarvest? {
        return diaryProvider.diary.harvests
            .firstOrNull { harvest ->
                harvest.id == identifiesHarvest.harvestId
            } ?: diaryProvider.diary.rejectedHarvests
            .firstOrNull { harvest ->
                harvest.id == identifiesHarvest.harvestId
            }
    }

    /**
     * Attempts to find a [GroupHuntingHarvest] that has the same id as specified by
     * given [identifiesHarvest]
     *
     * Depending on the value of [allowCached] may return already fetched harvest or
     * fetch all the necessary data and then try to find the harvest from fetched data.
     */
    suspend fun fetchHarvest(identifiesHarvest: IdentifiesGroupHuntingHarvest,
                             allowCached: Boolean): GroupHuntingHarvest? {
        if (allowCached) {
            findHarvest(identifiesHarvest)
                ?.let { harvest ->
                    return harvest
                }

            fetchAllData(refresh = false)
        } else {
            fetchAllData(refresh = true)
        }

        return findHarvest(identifiesHarvest)
    }

    suspend fun fetchHuntingDayForDeer(identifiesHuntingGroup: IdentifiesHuntingGroup, date: LocalDate)
            : GroupHuntingDayForDeerResponse {
        return huntingDayUpdater.fetchHuntingDayForDeer(identifiesHuntingGroup, date)
    }

    internal suspend fun createHarvest(harvest: CommonHarvestData): GroupHuntingHarvestOperationResponse {
        val createResponse = harvestUpdater.createHarvest(harvest)

        if (createResponse is GroupHuntingHarvestOperationResponse.Success) {
            diaryProvider.fetch(refresh = true)
        }

        return createResponse
    }

    /**
     * Attempts to accept given [harvest].
     *
     * This is a convenience function for updating the harvest
     * (accept harvest == update harvest)
     */
    suspend fun acceptHarvest(harvest: GroupHuntingHarvest): GroupHuntingHarvestOperationResponse {
        return updateHarvest(harvest)
    }

    suspend fun updateHarvest(harvest: GroupHuntingHarvest): GroupHuntingHarvestOperationResponse {
        val acceptResponse = harvestUpdater.updateHarvest(harvest)

        if (acceptResponse is GroupHuntingHarvestOperationResponse.Success) {
            // todo: consider replacing just one harvest instead of fetching all again
            diaryProvider.fetch(refresh = true)
        }

        return acceptResponse
    }

    suspend fun rejectHarvest(harvest: GroupHuntingHarvest): GroupHuntingHarvestOperationResponse {
        val rejectResponse = harvestUpdater.rejectHarvest(harvest)

        if (rejectResponse is GroupHuntingHarvestOperationResponse.Success) {
            // todo: consider replacing just one harvest instead of fetching all again
            diaryProvider.fetch(refresh = true)
        }

        return rejectResponse
    }

    /**
     * Attempts to find a [GroupHuntingObservation] that has the same id as specified by
     * given [identifiesObservation]
     */
    fun findObservation(identifiesObservation: IdentifiesGroupHuntingObservation): GroupHuntingObservation? {
        return diaryProvider.diary.observations
            .firstOrNull { observation ->
                observation.id == identifiesObservation.observationId
            } ?: diaryProvider.diary.rejectedObservations
            .firstOrNull { observation ->
                observation.id == identifiesObservation.observationId
            }
    }

    /**
     * Attempts to find a [GroupHuntingObservation] that has the same id as specified by
     * given [identifiesObservation]
     *
     * Depending on the value of [allowCached] may return already fetched observation or
     * fetch all the necessary data and then try to find the observation from fetched data.
     */
    suspend fun fetchObservation(identifiesObservation: IdentifiesGroupHuntingObservation,
                                 allowCached: Boolean): GroupHuntingObservation? {

        if (allowCached) {
            findObservation(identifiesObservation)
                ?.let { observation ->
                    return observation
                }

            fetchAllData(refresh = false)
        } else {
            fetchAllData(refresh = true)
        }

        return findObservation(identifiesObservation)
    }

    /**
     * Attempts to accept a given [GroupHuntingObservation]
     *
     * If successful, diary will be updated.
     */
    suspend fun acceptObservation(observation: GroupHuntingObservation): GroupHuntingObservationOperationResponse {
        val acceptResponse = observationUpdater.acceptObservation(observation)

        if (acceptResponse is GroupHuntingObservationOperationResponse.Success) {
            // todo: consider replacing just one observation instead of fetching all again
            diaryProvider.fetch(refresh = true)
        }

        return acceptResponse
    }

    /**
     * Attempts to reject a given [GroupHuntingObservation]
     *
     * If successful, diary will be updated.
     */
    suspend fun rejectObservation(observation: GroupHuntingObservation): GroupHuntingObservationOperationResponse {
        val rejectResponse = observationUpdater.rejectObservation(observation)

        if (rejectResponse is GroupHuntingObservationOperationResponse.Success) {
            // todo: consider replacing just one observation instead of fetching all again
            diaryProvider.fetch(refresh = true)
        }

        return rejectResponse
    }

    /**
     * Attempts to create a new [GroupHuntingObservation]
     *
     * If successful, diary will be updated.
     */
    suspend fun createObservation(observation: GroupHuntingObservationData): GroupHuntingObservationOperationResponse {
        val createResponse = observationUpdater.createObservation(observation)

        if (createResponse is GroupHuntingObservationOperationResponse.Success) {
            // todo: consider replacing just one observation instead of fetching all again
            diaryProvider.fetch(refresh = true)
        }

        return createResponse
    }

    /**
     * Attempts to find a [GroupHuntingDay] that has the same id as specified by
     * given [identifiesHuntingDay]
     */
    fun findHuntingDay(identifiesHuntingDay: IdentifiesGroupHuntingDay): GroupHuntingDay? {
        return huntingDaysProvider.huntingDays
            ?.firstOrNull { huntingDay ->
                huntingDay.id == identifiesHuntingDay.huntingDayId
            }
    }

    /**
     * Attempts to find a [GroupHuntingDay] that has the given date as start date.
     */
    fun findHuntingDay(startDate: LocalDate): GroupHuntingDay? {
        return huntingDaysProvider.huntingDays
            ?.firstOrNull { huntingDay ->
                huntingDay.startDateTime.date == startDate
            }
    }

    /**
     * Attempts to create a new [GroupHuntingDay] on the backend.
     *
     * The main purpose of this function is to coordinate work between [huntingDayUpdater] and
     * [huntingDaysProvider]
     */
    suspend fun createHuntingDay(
        huntingDay: GroupHuntingDay,
        currentTimeProvider: LocalDateTimeProvider
    ): GroupHuntingDayUpdateResponse {
        if (!huntingDay.isValid(huntingGroup.permit, currentTimeProvider)) {
            return GroupHuntingDayUpdateResponse.Error
        }

        // Mark hunting days to be updated upon next fetch. This way they get updated when
        // creating the hunting day succeeds on the backend but we fail to receive the response.
        // The next hunting day fetch will then fetch the days even if not explicitly refreshing.
        _huntingDaysProvider.shouldRefreshUponNextFetch()

        val updateResponse = huntingDayUpdater.createHuntingDay(huntingDay)

        if (updateResponse is GroupHuntingDayUpdateResponse.Updated) {
            huntingDaysProvider.fetch(refresh = true)
        }

        return updateResponse
    }

    /**
     * Attempts to update [GroupHuntingDay] on the backend.
     *
     * The main purpose of this function is to coordinate work between [huntingDayUpdater] and
     * [huntingDaysProvider]
     */
    suspend fun updateHuntingDay(
        huntingDay: GroupHuntingDay,
        currentTimeProvider: LocalDateTimeProvider
    ): GroupHuntingDayUpdateResponse {
        if (!huntingDay.isValid(huntingGroup.permit, currentTimeProvider)) {
            return GroupHuntingDayUpdateResponse.Error
        }

        // Mark hunting days to be updated upon next fetch. This way they get updated when
        // updating the hunting day succeeds on the backend but we fail to receive the response.
        // The next hunting day fetch will then fetch the days even if not explicitly refreshing.
        _huntingDaysProvider.shouldRefreshUponNextFetch()

        val updateResponse = huntingDayUpdater.updateHuntingDay(huntingDay)

        if (updateResponse is GroupHuntingDayUpdateResponse.Updated) {
            // todo: consider replacing just one hunting day instead of fetching all again
            huntingDaysProvider.fetch(refresh = true)
        }

        return updateResponse
    }

    /**
     * Attempts to find a [GroupHuntingDay] that has the same id as specified by the
     * given [identifiesHuntingDay].
     *
     * Depending on the value of [allowCached] may return already fetched hunting day or
     * fetch all the necessary data and then try to find the hunting day from fetched data.
     */
    suspend fun fetchHuntingDay(identifiesHuntingDay: IdentifiesGroupHuntingDay,
                                allowCached: Boolean): GroupHuntingDay? {
        if (allowCached) {
            findHuntingDay(identifiesHuntingDay)
                ?.let { huntingDay ->
                    return huntingDay
                }

            fetchAllData(refresh = false)
        } else {
            fetchAllData(refresh = true)
        }

        return findHuntingDay(identifiesHuntingDay)
    }

    /**
     * Attempts to find a [GroupHuntingDay] that has the same start date as specified by the
     * given [startDate].
     *
     * Depending on the value of [allowCached] may return already fetched hunting day or
     * fetch all the necessary data and then try to find the hunting day from fetched data.
     */
    suspend fun fetchHuntingDay(startDate: LocalDate, allowCached: Boolean): GroupHuntingDay? {
        if (allowCached) {
            findHuntingDay(startDate)
                ?.let { huntingDay ->
                    return huntingDay
                }

            fetchAllData(refresh = false)
        } else {
            fetchAllData(refresh = true)
        }

        return findHuntingDay(startDate)
    }

    private suspend fun fetchData(data: HuntingClubGroupDataPiece, refresh: Boolean) {
        when (data) {
            HuntingClubGroupDataPiece.MEMBERS -> {
                membersProvider.fetch(refresh)
            }
            HuntingClubGroupDataPiece.HUNTING_AREA -> {
                huntingAreaProvider.fetch(refresh)
            }
            HuntingClubGroupDataPiece.STATUS -> {
                huntingStatusProvider.fetch(refresh)
            }
            HuntingClubGroupDataPiece.HUNTING_DAYS -> {
                huntingDaysProvider.fetch(refresh)
            }
            HuntingClubGroupDataPiece.DIARY -> {
                diaryProvider.fetch(refresh)
            }
        }
    }
}
