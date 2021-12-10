package fi.riista.common.groupHunting.ui.groupHarvest.modify

import fi.riista.common.groupHunting.GroupHuntingClubGroupContext
import fi.riista.common.groupHunting.GroupHuntingContext
import fi.riista.common.groupHunting.GroupHuntingDayForDeerResponse
import fi.riista.common.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.groupHunting.model.*
import fi.riista.common.groupHunting.ui.groupHarvest.*
import fi.riista.common.logging.getLogger
import fi.riista.common.model.*
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A controller for accepting [GroupHuntingHarvest]s as well as editing already
 * accepted harvest information
 */
class EditGroupHarvestController(
    groupHuntingContext: GroupHuntingContext,
    private val harvestTarget: GroupHuntingHarvestTarget,
    stringProvider: StringProvider,
) : ModifyGroupHarvestController(groupHuntingContext, stringProvider) {

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
        val harvestDataWithHuntingDay = if (harvestData.gameSpeciesCode.isDeer()) {
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

        return operation(groupContext, harvestToBeAccepted)
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

        val statusProvider = groupContext.huntingStatusProvider
        statusProvider.fetch(refresh = refresh)
        val membersProvider = groupContext.membersProvider
        membersProvider.fetch(refresh = refresh)
        val huntingDaysProvider = groupContext.huntingDaysProvider
        huntingDaysProvider.fetch(refresh = refresh)

        val groupStatus = statusProvider.status
        val groupMembers = membersProvider.members
        val huntingDays = huntingDaysProvider.huntingDays

        if (groupStatus != null && groupMembers != null && huntingDays != null) {
            // use restored harvest values if harvest id it matches the id of the found harvest
            val harvestData = restoredHarvestData?.takeIf { it.id == fetchedHarvest.id }
                    ?: fetchedHarvest
                        .toGroupHuntingHarvestData(groupMembers = groupMembers)
                        .selectInitialHuntingDayForHarvest(huntingDays)
                        .ensureDefaultValuesAreSet()

            val viewModel = createViewModel(
                    harvest = harvestData,
                    huntingGroupStatus = groupStatus,
                    huntingGroupMembers = groupMembers,
                    huntingGroupPermit = groupContext.huntingGroup.permit,
                    huntingDays = huntingDays
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

