package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingDayForDeerResponse
import fi.riista.common.domain.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.domain.groupHunting.HuntingClubGroupDataPiece
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingPerson
import fi.riista.common.domain.groupHunting.model.IdentifiesHuntingGroup
import fi.riista.common.domain.groupHunting.model.coerceInPermitValidityPeriods
import fi.riista.common.domain.groupHunting.model.createTargetForHuntingDay
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.ui.modify.ModifyHarvestIntent
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.EntityImages
import fi.riista.common.domain.model.SearchableOrganization
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.domain.model.isDeer
import fi.riista.common.logging.getLogger
import fi.riista.common.model.BackendEnum
import fi.riista.common.model.ETRMSGeoLocation
import fi.riista.common.model.GeoLocationSource
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.isInsideFinland
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import fi.riista.common.util.generateMobileClientRefId
import fi.riista.common.util.toETRSCoordinate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A controller for creating new [GroupHuntingHarvest]s
 */
class CreateGroupHarvestController internal constructor(
    groupHuntingContext: GroupHuntingContext,
    private val huntingGroupTarget: IdentifiesHuntingGroup,
    localDateTimeProvider: LocalDateTimeProvider,
    speciesResolver: SpeciesResolver,
    stringProvider: StringProvider,
) : ModifyGroupHarvestController(groupHuntingContext, localDateTimeProvider, speciesResolver, stringProvider) {

    constructor(
        groupHuntingContext: GroupHuntingContext,
        huntingGroupTarget: IdentifiesHuntingGroup,
        speciesResolver: SpeciesResolver,
        stringProvider: StringProvider
    ): this(groupHuntingContext, huntingGroupTarget, SystemDateTimeProvider(), speciesResolver, stringProvider)

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ModifyGroupHarvestViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = huntingGroupTarget,
                allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to fetch the hunting group (id: ${huntingGroupTarget.huntingGroupId})" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val dataPieces = listOf(
            HuntingClubGroupDataPiece.STATUS,
            HuntingClubGroupDataPiece.MEMBERS,
            HuntingClubGroupDataPiece.HUNTING_DAYS,
            HuntingClubGroupDataPiece.HUNTING_AREA
        )
        groupContext.fetchDataPieces(dataPieces, refresh)

        val groupStatus = groupContext.huntingStatusProvider.status
        val groupMembers = groupContext.membersProvider.members
        val huntingDays = groupContext.huntingDaysProvider.huntingDays
        val huntingGroupArea = groupContext.huntingAreaProvider.area

        if (groupStatus != null && groupMembers != null &&
            huntingDays != null && huntingGroupArea != null) {

            val huntingGroupPermit = groupContext.huntingGroup.permit
            val harvestPointOfTime = localDateTimeProvider.now().let { now ->
                LocalDateTime(
                        date = now.date.coerceInPermitValidityPeriods(huntingGroupPermit),
                        time = now.time
                )
            }

            val areaCenterCoordinate = huntingGroupArea.bounds.centerCoordinate.toETRSCoordinate()
            val harvestData = restoredHarvestData ?: CommonHarvestData(
                    localId = null,
                    localUrl = null,
                    id = null,
                    rev = null,
                    species = Species.Known(groupContext.huntingGroup.speciesCode),
                    location = ETRMSGeoLocation(
                            // conversion to int should be safe assuming that hunting area is in Finland
                            // -> ETRS coordinates should be within int-range
                            latitude = areaCenterCoordinate.x.toInt(),
                            longitude = areaCenterCoordinate.y.toInt(),
                            source = GeoLocationSource.MANUAL.toBackendEnum(),
                            accuracy = null,
                            altitude = null,
                            altitudeAccuracy = null,
                    ).asKnownLocation(),
                    pointOfTime = harvestPointOfTime,
                    description = null,
                    images = EntityImages.noImages(),
                    specimens = listOf(
                        CommonSpecimenData().ensureDefaultValuesAreSet()
                    ),
                    amount = null, // don't set initial value for _group harvests_
                    huntingDayId = null,
                    actorInfo = GroupHuntingPerson.Unknown,
                    authorInfo = null, // will be set on the backend
                    selectedClub = SearchableOrganization.Unknown,
                    canEdit = true,
                    modified = true,
                    deleted = false,
                    harvestSpecVersion = Constants.HARVEST_SPEC_VERSION,
                    harvestReportRequired = false,
                    harvestReportState = BackendEnum.create(null),
                    permitNumber = null,
                    permitType = null,
                    stateAcceptedToHarvestPermit = BackendEnum.create(null),
                    deerHuntingType = BackendEnum.create(null),
                    deerHuntingOtherTypeDescription = null,
                    mobileClientRefId = generateMobileClientRefId(),
                    harvestReportDone = false,
                    rejected = false,
                    feedingPlace = null,
                    taigaBeanGoose = null,
                    greySealHuntingMethod = BackendEnum.create(null),
            )

            val viewModel = createViewModel(
                    harvest = harvestData.selectInitialHuntingDayForHarvest(huntingDays),
                    huntingGroupStatus = groupStatus,
                    huntingGroupMembers = groupMembers,
                    huntingGroupPermit = huntingGroupPermit,
                    huntingDays = huntingDays,
                    huntingGroupArea = huntingGroupArea
            ).applyPendingIntents()

            emit(ViewModelLoadStatus.Loaded(viewModel))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    suspend fun createHarvest(): GroupHuntingHarvestOperationResponse {
        val harvestData = getLoadedViewModelOrNull()?.getValidatedHarvestDataOrNull() ?: kotlin.run {
            logger.w { "Failed to obtain validated harvest from viewModel in order to create it" }
            return GroupHuntingHarvestOperationResponse.Error
        }

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
            identifiesClubAndGroup = huntingGroupTarget,
            allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to obtain group context in order to create a harvest" }
            return GroupHuntingHarvestOperationResponse.Error
        }

        // If deer, get huntingDayId from backend
        val harvestDataWithHuntingDay = if (harvestData.species.isDeer()) {
            when (val huntingDayResponse = groupContext.fetchHuntingDayForDeer(huntingGroupTarget, harvestData.pointOfTime.date)) {
                is GroupHuntingDayForDeerResponse.Failed -> {
                    logger.w { "Unable to fetch hunting day for deer" }
                    return GroupHuntingHarvestOperationResponse.Failure(huntingDayResponse.networkStatusCode)
                }
                is GroupHuntingDayForDeerResponse.Success -> {
                    harvestData.copy(huntingDayId = huntingDayResponse.huntingDay.id)
                }
            }
        } else {
            harvestData
        }

        return groupContext.createHarvest(harvestDataWithHuntingDay)
    }

    override fun findHuntingDay(huntingDayId: GroupHuntingDayId): GroupHuntingDay? {
        val dayTarget = huntingGroupTarget.createTargetForHuntingDay(huntingDayId)
        return groupHuntingContext.findGroupHuntingDay(dayTarget)
    }

    /**
     * Can the harvest be moved to current user location?
     *
     * Harvest is not allowed to be moved automatically if its location has been updated
     * manually by the user.
     */
    fun canMoveHarvestToCurrentUserLocation(): Boolean {
        return harvestLocationCanBeUpdatedAutomatically
    }

    /**
     * Tries to move the harvest to the current user location.
     *
     * Controller will only update the location of the harvest if user has not explicitly
     * set the location previously and if the given [location] is inside Finland.
     *
     * The return value indicates whether it might be possible to update harvest location to
     * current user location in the future.
     *
     * @return  True if harvest location can be changed in the future, false otherwise.
     */
    fun tryMoveHarvestToCurrentUserLocation(location: ETRMSGeoLocation): Boolean {
        if (!location.isInsideFinland()) {
            // It is possible that the exact GPS location is not yet known and the attempted
            // location is outside of Finland. Don't prevent future updates because of this.
            return true
        }

        if (!canMoveHarvestToCurrentUserLocation()) {
            return false
        }

        // don't allow automatic location updates unless the harvest really is loaded
        // - otherwise we might get pending location updates which could become disallowed
        //   when the harvest is being loaded
        if (getLoadedViewModelOrNull() == null) {
            return true
        }

        handleIntent(
            ModifyHarvestIntent.ChangeLocation(
                newLocation = location,
                locationChangedAfterUserInteraction = false
        ))
        return true
    }

    companion object {
        private val logger by getLogger(CreateGroupHarvestController::class)
    }
}

