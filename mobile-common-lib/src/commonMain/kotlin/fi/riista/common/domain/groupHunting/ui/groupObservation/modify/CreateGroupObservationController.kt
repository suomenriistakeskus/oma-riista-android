package fi.riista.common.domain.groupHunting.ui.groupObservation.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.dto.toPersonWithHunterNumber
import fi.riista.common.domain.groupHunting.GroupHuntingClubGroupContext
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingObservationOperationResponse
import fi.riista.common.domain.groupHunting.HuntingClubGroupDataPiece
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.ui.groupObservation.GroupObservationIntent
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.ObservationCategory
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.logging.getLogger
import fi.riista.common.model.*
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import fi.riista.common.util.generateMobileClientRefId
import fi.riista.common.util.toETRSCoordinate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CreateGroupObservationController(
    private val groupHuntingContext: GroupHuntingContext,
    private val huntingGroupTarget: IdentifiesHuntingGroup,
    private val sourceHarvestTarget: GroupHuntingHarvestTarget?,
    stringProvider: StringProvider,
) : ModifyGroupObservationController(stringProvider) {

    private val localDateTimeProvider: LocalDateTimeProvider = SystemDateTimeProvider()

    suspend fun createObservation(): GroupHuntingObservationOperationResponse {
        val observationData = getLoadedViewModelOrNull()?.observation ?: kotlin.run {
            logger.w { "Failed to obtain observation from viewModel in order to create it" }
            return GroupHuntingObservationOperationResponse.Error
        }

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
            identifiesClubAndGroup = huntingGroupTarget,
            allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to obtain group context in order to accept an observation" }
            return GroupHuntingObservationOperationResponse.Error
        }

        val observationToBeCreated = createObservationToBeAccepted(observationData)
        return groupContext.createObservation(observationToBeCreated)
    }

    /**
     * Creates a new [GroupHuntingObservationData] based on given [observation] data that contains only those
     * fields that should be saved.
     */
    private fun createObservationToBeAccepted(observation: GroupHuntingObservationData): GroupHuntingObservationData {
        return observation.copy(mooselikeFemale4CalfsAmount =
            if (observation.gameSpeciesCode == SpeciesCodes.WHITE_TAILED_DEER_ID) {
                observation.mooselikeFemale4CalfsAmount
            } else {
                null
            }
        )
    }

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ModifyGroupObservationViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
            identifiesClubAndGroup = huntingGroupTarget,
            allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to fetch the hunting group (id: ${huntingGroupTarget.huntingGroupId})" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        groupContext.fetchDataPieces(
                dataPieces = listOf(
                        HuntingClubGroupDataPiece.STATUS,
                        HuntingClubGroupDataPiece.MEMBERS,
                        HuntingClubGroupDataPiece.HUNTING_AREA,
                        HuntingClubGroupDataPiece.HUNTING_DAYS,
                ),
                refresh = refresh
        )

        val groupStatus = groupContext.huntingStatusProvider.status
        val groupMembers = groupContext.membersProvider.members
        val huntingGroupArea = groupContext.huntingAreaProvider.area
        val huntingDays = groupContext.huntingDaysProvider.huntingDays ?: listOf()

        if (groupStatus != null && groupMembers != null && huntingGroupArea != null) {
            val huntingGroupPermit = groupContext.huntingGroup.permit

            val observation = restoredObservation
                ?: getObservationFromHarvest(sourceHarvestTarget, groupMembers)?.also {
                    // observation was created based on harvest and thus we know the exact location.
                    // -> don't allow moving observation location automatically anywhere else
                    observationLocationCanBeMovedAutomatically.set(false)
                }
                ?: createEmptyObservation(groupContext, huntingGroupArea, huntingDays, huntingGroupPermit).also {
                    // totally new observation, location can be updated automatically
                    observationLocationCanBeMovedAutomatically.set(true)
                }

            val viewModel = createViewModel(
                observation = observation,
                huntingGroupMembers = groupMembers,
                huntingDays = huntingDays,
                huntingGroupArea = huntingGroupArea,
            ).applyPendingIntents()

            emit(ViewModelLoadStatus.Loaded(viewModel))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    override fun findHuntingDay(huntingDayId: GroupHuntingDayId): GroupHuntingDay? {
        val dayTarget = huntingGroupTarget.createTargetForHuntingDay(huntingDayId)
        return groupHuntingContext.findGroupHuntingDay(dayTarget)
    }

    override suspend fun searchPersonByHunterNumber(hunterNumber: HunterNumber): PersonWithHunterNumber? {
        return groupHuntingContext.searchPersonByHunterNumber(hunterNumber)?.toPersonWithHunterNumber()
    }

    /**
     * Can the observation be moved to current user location?
     *
     * Observation is not allowed to be moved automatically if its location has been updated
     * manually by the user.
     */
    fun canMoveObservationToCurrentUserLocation(): Boolean {
        return observationLocationCanBeMovedAutomatically.value == true
    }

    /**
     * Tries to move the observation to the current user location.
     *
     * Controller will only update the location of the observation if user has not explicitly
     * set the location previously and if the given [location] is inside Finland.
     *
     * The return value indicates whether it might be possible to update observation location to
     * current user location in the future.
     *
     * @return  True if observation location can be changed in the future, false otherwise.
     */
    fun tryMoveObservationToCurrentUserLocation(location: ETRMSGeoLocation): Boolean {
        if (!location.isInsideFinland()) {
            // It is possible that the exact GPS location is not yet known and the attempted
            // location is outside of Finland. Don't prevent future updates because of this.
            return true
        }

        if (!canMoveObservationToCurrentUserLocation()) {
            return false
        }

        // don't allow automatic location updates unless the observation really is loaded
        // - otherwise we might get pending location updates which could become disallowed
        //   when the observation is being loaded
        if (getLoadedViewModelOrNull() == null) {
            return true
        }

        handleIntent(GroupObservationIntent.ChangeLocation(
                newLocation = location,
                locationChangedAfterUserInteraction = false
        ))
        return true
    }

    private fun createEmptyObservation(
        groupContext: GroupHuntingClubGroupContext,
        area: HuntingGroupArea,
        huntingDays: List<GroupHuntingDay>,
        huntingGroupPermit: HuntingGroupPermit,
    ): GroupHuntingObservationData {
        val observationPointOfTime = localDateTimeProvider.now().let { now ->
            LocalDateTime(
                    date = now.date.coerceInPermitValidityPeriods(huntingGroupPermit),
                    time = now.time
            )
        }

        val areaCenterCoordinate = area.bounds.centerCoordinate.toETRSCoordinate()

        return GroupHuntingObservationData(
            gameSpeciesCode = groupContext.huntingGroup.speciesCode,
            geoLocation = ETRMSGeoLocation(
                // conversion to int should be safe assuming that hunting area is in Finland
                // -> ETRS coordinates should be within int-range
                latitude = areaCenterCoordinate.x.toInt(),
                longitude = areaCenterCoordinate.y.toInt(),
                source = GeoLocationSource.MANUAL.toBackendEnum(),
                accuracy = null,
                altitude = null,
                altitudeAccuracy = null,
            ),
            pointOfTime = observationPointOfTime,
            canEdit = true,
            imageIds = listOf(),
            specimens = listOf(),
            huntingDayId = null,
            actorInfo = GroupHuntingPerson.Unknown,
            authorInfo = null, // will be set on the backend
            linkedToGroupHuntingDay = false,
            observationType = BackendEnum.create(ObservationType.NAKO),
            observationCategory = BackendEnum.create(ObservationCategory.MOOSE_HUNTING),
            mobileClientRefId = generateMobileClientRefId(),
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            deerHuntingType = BackendEnum.create(null),
            mooselikeMaleAmount = 0,
            mooselikeFemaleAmount = 0,
            mooselikeCalfAmount = 0,
            mooselikeFemale1CalfAmount = 0,
            mooselikeFemale2CalfsAmount = 0,
            mooselikeFemale3CalfsAmount = 0,
            mooselikeFemale4CalfsAmount = 0,
            mooselikeUnknownSpecimenAmount = 0,
            totalSpecimenAmount = 0,
            rejected = false,
        ).selectInitialHuntingDayForObservation(huntingDays)
    }

    private suspend fun getObservationFromHarvest(
        harvestTarget: GroupHuntingHarvestTarget?,
        groupMembers: List<HuntingGroupMember>,
    ): GroupHuntingObservationData? {
        val harvest = groupHuntingContext.fetchGroupHuntingHarvest(harvestTarget, true)
        if (harvest != null) {
            return convertHarvestToObservation(harvest, groupMembers)
        }
        return null
    }

    private fun convertHarvestToObservation(
        harvest: GroupHuntingHarvest,
        groupMembers: List<HuntingGroupMember>,
    ): GroupHuntingObservationData {
        return GroupHuntingObservationData(
            gameSpeciesCode = harvest.gameSpeciesCode,
            geoLocation = harvest.geoLocation,
            pointOfTime = harvest.pointOfTime,
            canEdit = true,
            imageIds = listOf(),
            specimens = listOf(),
            huntingDayId = harvest.huntingDayId,
            actorInfo = harvest.actorInfo.let { actor ->
                if (groupMembers.isMember(actor)) {
                    actor.asGroupMember()
                } else {
                    actor.asGuest()
                }
            },
            authorInfo = null, // will be set on the backend
            linkedToGroupHuntingDay = false,
            observationType = BackendEnum.create(ObservationType.NAKO),
            observationCategory = BackendEnum.create(ObservationCategory.MOOSE_HUNTING),
            mobileClientRefId = generateMobileClientRefId(),
            observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION,
            deerHuntingType = BackendEnum.create(null),
            mooselikeMaleAmount = SpecimenCounter.adultMaleAmount(harvest.specimens),
            mooselikeFemaleAmount = SpecimenCounter.adultFemaleAmount(harvest.specimens),
            mooselikeCalfAmount = SpecimenCounter.aloneCalfAmount(harvest.specimens),
            mooselikeFemale1CalfAmount = 0,
            mooselikeFemale2CalfsAmount = 0,
            mooselikeFemale3CalfsAmount = 0,
            mooselikeFemale4CalfsAmount = 0,
            mooselikeUnknownSpecimenAmount = 0,
            totalSpecimenAmount = 0,
            rejected = false,
        )
    }

    companion object {
        private val logger by getLogger(CreateGroupObservationController::class)
    }
}
