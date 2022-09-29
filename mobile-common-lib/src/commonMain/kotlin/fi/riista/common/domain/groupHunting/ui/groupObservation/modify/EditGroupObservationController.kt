package fi.riista.common.domain.groupHunting.ui.groupObservation.modify

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.dto.toPersonWithHunterNumber
import fi.riista.common.domain.groupHunting.GroupHuntingClubGroupContext
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingObservationOperationResponse
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.model.HunterNumber
import fi.riista.common.domain.model.PersonWithHunterNumber
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EditGroupObservationController(
    private val groupHuntingContext: GroupHuntingContext,
    private val observationTarget: IdentifiesGroupHuntingObservation,
    stringProvider: StringProvider,
) : ModifyGroupObservationController(stringProvider) {

    private var huntingGroup: GroupHuntingClubGroupContext? = null

    suspend fun acceptObservation(): GroupHuntingObservationOperationResponse {
        val loadedState = viewModelLoadStatus.value.loadedViewModel

        val observation = loadedState?.observation?.toGroupHuntingObservation()
            ?: kotlin.run {
                logger.w { "Failed to obtain observation from viewModel in order to accept it" }
                return GroupHuntingObservationOperationResponse.Error
            }

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
            identifiesClubAndGroup = observationTarget,
            allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to obtain group context in order to accept an observation" }
            return GroupHuntingObservationOperationResponse.Error
        }


        // Update spec version to current one. Needed in case we are editing an old observation.
        val observationToBeAccepted = observation.copy(observationSpecVersion = Constants.OBSERVATION_SPEC_VERSION)
        return groupContext.acceptObservation(observationToBeAccepted)
    }

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ModifyGroupObservationViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
            identifiesClubAndGroup = observationTarget,
            allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to load a context for the hunting group ${observationTarget.huntingGroupId}" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val fetchedObservation = groupContext.fetchObservation(
            identifiesObservation = observationTarget,
            allowCached = true
        )

        val club = groupHuntingContext.fetchClubContext(
            identifiesHuntingClub = GroupHuntingClubTarget(observationTarget.clubId),
            allowCached = true
        )

        huntingGroup = club?.findHuntingGroupContext(
            HuntingGroupTarget(
            clubId = observationTarget.clubId,
            huntingGroupId = observationTarget.huntingGroupId)
        )
        huntingGroup?.fetchAllData(refresh = refresh)

        val statusProvider = groupContext.huntingStatusProvider
        val groupMembers = groupContext.membersProvider.members
        val huntingDays = groupContext.huntingDaysProvider.huntingDays ?: listOf()

        if (fetchedObservation == null || huntingGroup == null || groupMembers == null || !statusProvider.loadStatus.value.loaded) {
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        // use restored observation values if its id matches the id of the found observation
        val observationData = restoredObservation?.takeIf { it.id == fetchedObservation.id }
            ?: fetchedObservation
                .toGroupHuntingObservationData(groupMembers = groupMembers)
                .selectInitialHuntingDayForObservation(huntingDays)

        val viewModel = createViewModel(
            observation = observationData,
            huntingGroupMembers = groupMembers,
            huntingDays = huntingDays,
            huntingGroupArea = groupContext.huntingAreaProvider.area,
        ).applyPendingIntents()

        emit(ViewModelLoadStatus.Loaded(viewModel))
    }

    override fun findHuntingDay(huntingDayId: GroupHuntingDayId): GroupHuntingDay? {
        val dayTarget = observationTarget.createTargetForHuntingDay(huntingDayId)
        return groupHuntingContext.findGroupHuntingDay(dayTarget)
    }

    override suspend fun searchPersonByHunterNumber(hunterNumber: HunterNumber): PersonWithHunterNumber? {
        return groupHuntingContext.searchPersonByHunterNumber(hunterNumber)?.toPersonWithHunterNumber()
    }

    companion object {
        private val logger by getLogger(EditGroupObservationController::class)
    }
}
