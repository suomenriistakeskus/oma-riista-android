package fi.riista.common.domain.groupHunting.ui.groupHarvest.view


import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.domain.groupHunting.HuntingClubGroupDataPiece
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvest
import fi.riista.common.domain.groupHunting.model.GroupHuntingHarvestTarget
import fi.riista.common.domain.groupHunting.model.HuntingGroupArea
import fi.riista.common.domain.groupHunting.model.toCommonHarvestData
import fi.riista.common.domain.groupHunting.model.toGroupHuntingHarvest
import fi.riista.common.domain.groupHunting.ui.HarvestActionResolver
import fi.riista.common.domain.groupHunting.ui.groupHarvest.GroupHuntingHarvestFields
import fi.riista.common.domain.harvest.model.CommonHarvest
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.view.ViewHarvestFieldProducer
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * A controller for viewing [CommonHarvest] information
 */
@Suppress("MemberVisibilityCanBePrivate")
class ViewGroupHarvestController(
    private val groupHuntingContext: GroupHuntingContext,
    private val harvestTarget: GroupHuntingHarvestTarget,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<ViewGroupHarvestViewModel>() {

    private val dataFieldProducer: ViewHarvestFieldProducer by lazy {
        ViewHarvestFieldProducer(
            harvestPermitProvider = null,
            stringProvider = stringProvider,
            languageProvider = null
        )
    }

    /**
     * Loads the [GroupHuntingHarvest] and updates the [viewModelLoadStatus] accordingly.
     */
    suspend fun loadHarvest() {
        val loadFlow = createLoadViewModelFlow(refresh = false)

        loadFlow.collect { viewModelLoadStatus ->
            updateViewModel(viewModelLoadStatus)
        }
    }

    suspend fun rejectHarvest(): GroupHuntingHarvestOperationResponse {
        val viewModel = viewModelLoadStatus.value.loadedViewModel
                ?: run {
                    logger.w { "Failed to obtain loaded viewmodel for handling intent" }
                    return GroupHuntingHarvestOperationResponse.Error
                }

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = harvestTarget,
                allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to obtain group context in order to reject a harvest" }
            return GroupHuntingHarvestOperationResponse.Error
        }

        val harvest = viewModel.harvestData.toGroupHuntingHarvest()
            ?: kotlin.run {
                logger.w { "Failed to create harvest from harvest data in order to reject a harvest" }
                return GroupHuntingHarvestOperationResponse.Error
            }

        return groupContext.rejectHarvest(harvest)
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ViewGroupHarvestViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)


        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
                identifiesClubAndGroup = harvestTarget,
                allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to load a context for the hunting group ${harvestTarget.huntingGroupId}" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val harvest = groupContext.fetchHarvest(
                identifiesHarvest = harvestTarget,
                allowCached = !refresh
        )

        val dataPieces = listOf(
            HuntingClubGroupDataPiece.STATUS,
            HuntingClubGroupDataPiece.MEMBERS,
            HuntingClubGroupDataPiece.HUNTING_AREA,
        )
        groupContext.fetchDataPieces(dataPieces, refresh)

        val huntingGroupStatus = groupContext.huntingStatusProvider.status
        val groupMembers = groupContext.membersProvider.members ?: listOf()

        if (harvest != null && huntingGroupStatus != null) {
            val harvestData = harvest.toCommonHarvestData(groupMembers)

            emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    harvestData = harvestData,
                    canEditHarvest = HarvestActionResolver.canEditHarvest(huntingGroupStatus, harvestData),
                    canApproveHarvest = HarvestActionResolver.canApproveHarvest(huntingGroupStatus, harvestData),
                    canRejectHarvest = HarvestActionResolver.canRejectHarvest(huntingGroupStatus, harvestData),
                    huntingGroupArea = groupContext.huntingAreaProvider.area,
                )
            ))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    private fun createViewModel(
        harvestData: CommonHarvestData,
        canEditHarvest: Boolean,
        canApproveHarvest: Boolean,
        canRejectHarvest: Boolean,
        huntingGroupArea: HuntingGroupArea?,
    ): ViewGroupHarvestViewModel {
        return ViewGroupHarvestViewModel(
            harvestData = harvestData,
            fields = produceDataFields(harvestData),
            canEditHarvest = canEditHarvest,
            canApproveHarvest = canApproveHarvest,
            canRejectHarvest = canRejectHarvest,
            huntingGroupArea = huntingGroupArea,
        )
    }

    private fun produceDataFields(harvestData: CommonHarvestData): List<DataField<CommonHarvestField>> {
        val fieldsToBeDisplayed = GroupHuntingHarvestFields.getFieldsToBeDisplayed(
            GroupHuntingHarvestFields.Context(
                harvest = harvestData,
                mode = GroupHuntingHarvestFields.Context.Mode.VIEW
            )
        )

        return fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
            dataFieldProducer.createField(
                fieldSpecification = fieldSpecification,
                harvest = harvestData,
            )
        }
    }

    companion object {
        private val logger by getLogger(ViewGroupHarvestController::class)
    }
}

