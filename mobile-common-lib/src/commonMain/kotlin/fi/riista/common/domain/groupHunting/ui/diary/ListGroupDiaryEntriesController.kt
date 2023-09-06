package fi.riista.common.domain.groupHunting.ui.diary

import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.HuntingClubGroupDataPiece
import fi.riista.common.domain.groupHunting.model.DiaryEntryType
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestId
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservation
import fi.riista.common.domain.groupHunting.model.GroupHuntingObservationId
import fi.riista.common.domain.groupHunting.model.HuntingGroupTarget
import fi.riista.common.domain.groupHunting.model.createTargetForHarvest
import fi.riista.common.domain.groupHunting.model.createTargetForObservation
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * A controller for providing a list of selected [GroupHuntingHarvest]s and
 * [GroupHuntingObservation]s.
 */
class ListGroupDiaryEntriesController(
    private val groupHuntingContext: GroupHuntingContext,
    private val huntingGroupTarget: HuntingGroupTarget,
) : ControllerWithLoadableModel<ListGroupDiaryEntriesViewModel>() {

    var harvestIds: List<GroupHuntingHarvestId> = listOf()
    var observationIds: List<GroupHuntingObservationId> = listOf()

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ListGroupDiaryEntriesViewModel>> = flow {

        emit(ViewModelLoadStatus.Loading)

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = huntingGroupTarget,
                allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to load a context for the hunting group ${huntingGroupTarget.huntingGroupId}" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        groupContext.fetchDataPieces(
                dataPieces = listOf(
                        HuntingClubGroupDataPiece.DIARY
                ),
                refresh = refresh
        )

        val harvestEntries = harvestIds.mapNotNull { harvestId ->
            groupContext.findHarvest(huntingGroupTarget.createTargetForHarvest(harvestId))
                ?.let { harvest ->
                    GroupDiaryEntryViewModel(
                            type = DiaryEntryType.HARVEST,
                            remoteId = harvestId,
                            speciesCode = harvest.gameSpeciesCode,
                            acceptStatus = harvest.acceptStatus,
                            pointOfTime = harvest.pointOfTime,
                            actorName = harvest.actorInfo.let {
                                "${it.byName} ${it.lastName}"
                            }
                )
            }
        }
        val observationEntries = observationIds.mapNotNull { observationId ->
            groupContext.findObservation(huntingGroupTarget.createTargetForObservation(observationId))
                ?.let { observation ->
                    GroupDiaryEntryViewModel(
                            type = DiaryEntryType.OBSERVATION,
                            remoteId = observationId,
                            speciesCode = observation.gameSpeciesCode,
                            acceptStatus = observation.acceptStatus,
                            pointOfTime = observation.pointOfTime,
                            actorName = observation.actorInfo.let {
                                "${it.byName} ${it.lastName}"
                            }
                    )
                }
        }

        emit(ViewModelLoadStatus.Loaded(
                viewModel = ListGroupDiaryEntriesViewModel(
                        entries = (harvestEntries + observationEntries).sortedByDescending {
                            it.pointOfTime.toStringISO8601() + "_${it.type}_${it.remoteId}"
                        }
                )
        ))
    }

    companion object {
        private val logger by getLogger(ListGroupDiaryEntriesController::class)
    }
}

