package fi.riista.common.domain.groupHunting.ui.groupHarvest.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.groupHunting.GroupHuntingClubGroupContext
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingDayForDeerResponse
import fi.riista.common.domain.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.domain.groupHunting.HuntingClubGroupDataPiece
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.IdentifiesGroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.createTargetForHuntingDay
import fi.riista.common.domain.groupHunting.model.toCommonHarvestData
import fi.riista.common.domain.groupHunting.model.toGroupHuntingHarvest
import fi.riista.common.domain.model.isDeer
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.SystemDateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A controller for accepting [GroupHuntingHarvest]s as well as editing already
 * accepted harvest information
 */
class EditGroupHarvestController(
    groupHuntingContext: GroupHuntingContext,
    private val harvestTarget: IdentifiesGroupHuntingHarvest,
    localDateTimeProvider: LocalDateTimeProvider,
    speciesResolver: SpeciesResolver,
    stringProvider: StringProvider,
) : ModifyGroupHarvestController(groupHuntingContext, localDateTimeProvider, speciesResolver, stringProvider) {

    constructor(
        groupHuntingContext: GroupHuntingContext,
        harvestTarget: IdentifiesGroupHuntingHarvest,
        speciesResolver: SpeciesResolver,
        stringProvider: StringProvider,
    ): this(groupHuntingContext, harvestTarget, SystemDateTimeProvider(), speciesResolver, stringProvider)

    suspend fun acceptHarvest(): GroupHuntingHarvestOperationResponse {
        return performHarvestOperation { groupContext, harvest ->
            groupContext.acceptHarvest(harvest)
        }
    }

    suspend fun updateHarvest(): GroupHuntingHarvestOperationResponse {
        return performHarvestOperation { groupContext, harvest ->
            groupContext.updateHarvest(harvest)
        }
    }

    private suspend fun performHarvestOperation(
        operation: suspend (GroupHuntingClubGroupContext, GroupHuntingHarvest) -> GroupHuntingHarvestOperationResponse
    ): GroupHuntingHarvestOperationResponse {
        val harvestData = getLoadedViewModelOrNull()?.getValidatedHarvestDataOrNull() ?: kotlin.run {
            logger.w { "Failed to obtain validated harvest data from viewModel in order to accept it" }
            return GroupHuntingHarvestOperationResponse.Error
        }

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
            identifiesClubAndGroup = harvestTarget,
            allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to obtain group context in order to accept a harvest" }
            return GroupHuntingHarvestOperationResponse.Error
        }

        // If deer, get huntingDayId from backend
        val harvestDataWithHuntingDay = if (harvestData.species.isDeer()) {
            when (val huntingDayResponse = groupContext.fetchHuntingDayForDeer(harvestTarget, harvestData.pointOfTime.date)) {
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

        val harvestToBeAccepted = harvestDataWithHuntingDay.toGroupHuntingHarvest()
                ?: kotlin.run {
                    logger.w { "Failed to convert harvest data to harvest!" }
                    return GroupHuntingHarvestOperationResponse.Error
                }

        // Update spec version to current one. Needed in case we are editing an old harvest.
        val harvest = harvestToBeAccepted.copy(harvestSpecVersion = Constants.HARVEST_SPEC_VERSION)
        return operation(groupContext, harvest)
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ModifyGroupHarvestViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)


        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = harvestTarget,
                allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to fetch the hunting group (id: ${harvestTarget.huntingGroupId}" +
                    " containing the harvest!" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val fetchedHarvest = groupContext.fetchHarvest(
                identifiesHarvest = harvestTarget,
                allowCached = !refresh
        ) ?: kotlin.run {
            logger.w { "Failed to fetch the harvest (id: ${harvestTarget.harvestId}) the harvest!" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        groupContext.fetchDataPieces(listOf(
            HuntingClubGroupDataPiece.STATUS,
            HuntingClubGroupDataPiece.MEMBERS,
            HuntingClubGroupDataPiece.HUNTING_DAYS,
            HuntingClubGroupDataPiece.HUNTING_AREA,
        ), refresh = refresh)

        val groupStatus = groupContext.huntingStatusProvider.status
        val groupMembers = groupContext.membersProvider.members
        val huntingDays = groupContext.huntingDaysProvider.huntingDays

        if (groupStatus != null && groupMembers != null && huntingDays != null) {
            // use restored harvest values if harvest id it matches the id of the found harvest
            val harvestData = restoredHarvestData?.takeIf { it.id == fetchedHarvest.id }
                    ?: fetchedHarvest
                        .toCommonHarvestData(groupMembers = groupMembers)
                        .selectInitialHuntingDayForHarvest(huntingDays)
                        .ensureDefaultValuesAreSet()

            val viewModel = createViewModel(
                    harvest = harvestData,
                    huntingGroupStatus = groupStatus,
                    huntingGroupMembers = groupMembers,
                    huntingGroupPermit = groupContext.huntingGroup.permit,
                    huntingDays = huntingDays,
                    huntingGroupArea = groupContext.huntingAreaProvider.area,
            ).applyPendingIntents()

            emit(ViewModelLoadStatus.Loaded(viewModel))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    override fun findHuntingDay(huntingDayId: GroupHuntingDayId): GroupHuntingDay? {
        val dayTarget = harvestTarget.createTargetForHuntingDay(huntingDayId)
        return groupHuntingContext.findGroupHuntingDay(dayTarget)
    }

    companion object {
        private val logger by getLogger(EditGroupHarvestController::class)
    }
}

