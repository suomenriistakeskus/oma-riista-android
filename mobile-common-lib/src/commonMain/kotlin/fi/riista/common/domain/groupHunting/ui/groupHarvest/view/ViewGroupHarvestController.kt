package fi.riista.common.domain.groupHunting.ui.groupHarvest.view

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingHarvestOperationResponse
import fi.riista.common.domain.groupHunting.HuntingClubGroupDataPiece
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.ui.GroupHarvestField
import fi.riista.common.domain.groupHunting.ui.HarvestActionResolver
import fi.riista.common.domain.groupHunting.ui.groupHarvest.GroupHuntingHarvestFields
import fi.riista.common.domain.model.asKnownLocation
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


/**
 * A controller for viewing [GroupHuntingHarvest] information
 */
@Suppress("MemberVisibilityCanBePrivate")
class ViewGroupHarvestController(
    private val groupHuntingContext: GroupHuntingContext,
    private val harvestTarget: GroupHuntingHarvestTarget,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<ViewGroupHarvestViewModel>() {

    private lateinit var dataFieldProducers: Map<
            GroupHarvestField,
            DataFieldProducer<GroupHuntingHarvestData, GroupHarvestField>
            >

    init {
        // should be accessed from UI thread only
        ensureNeverFrozen()

        initializeFieldProducers()
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
            val harvestData = harvest.toGroupHuntingHarvestData(groupMembers)

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
        harvestData: GroupHuntingHarvestData,
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

    private fun produceDataFields(harvestData: GroupHuntingHarvestData): List<DataField<GroupHarvestField>> {
        val fieldsToBeDisplayed = GroupHuntingHarvestFields.getFieldsToBeDisplayed(
                GroupHuntingHarvestFields.Context(
                        harvest = harvestData,
                        mode = GroupHuntingHarvestFields.Context.Mode.VIEW
                )
        )

        return fieldsToBeDisplayed.mapNotNull { fieldSpecification ->
            val producer = dataFieldProducers[fieldSpecification.fieldId]
                    ?: throw RuntimeException("Couldn't find producer for field ${fieldSpecification.fieldId}")

            producer.produceDataField(harvestData)
        }
    }

    private fun initializeFieldProducers() {
        dataFieldProducers = GroupHarvestField.values().mapNotNull { field ->
            when (field) {
                GroupHarvestField.LOCATION -> field to { harvest: GroupHuntingHarvestData ->
                    LocationField(field, harvest.geoLocation.asKnownLocation()) {
                        readOnly = true
                    }
                }
                GroupHarvestField.SPECIES_CODE -> field to { harvest: GroupHuntingHarvestData ->
                    SpeciesField(field, harvest.gameSpeciesCode) {
                        readOnly = true
                    }
                }
                GroupHarvestField.DATE_AND_TIME -> field to { harvest: GroupHuntingHarvestData ->
                    DateAndTimeField(field, harvest.pointOfTime) {
                        readOnly = true
                    }
                }
                GroupHarvestField.DEER_HUNTING_TYPE -> field to { harvest: GroupHuntingHarvestData ->
                    val deerHuntingTypeValue = harvest.deerHuntingType.value?.resourcesStringId
                        ?.let { stringId ->
                            stringProvider.getString(stringId)
                        } ?: harvest.deerHuntingType.rawBackendEnumValue

                    deerHuntingTypeValue?.createValueField(
                            fieldId = field,
                            label = RR.string.group_hunting_harvest_field_deer_hunting_type
                    )
                }
                GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION -> field to { harvest: GroupHuntingHarvestData ->
                    val description = harvest.deerHuntingOtherTypeDescription ?: "-"

                    description.createValueField(
                            fieldId = field,
                            label = RR.string.group_hunting_harvest_field_deer_hunting_other_type_description
                    )
                }
                GroupHarvestField.ACTOR -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.actorInfo.personWithHunterNumber
                        ?.let { "${it.byName} ${it.lastName}" }
                        ?.createValueField(
                            fieldId = field,
                            label = RR.string.group_hunting_harvest_field_actor
                        ) {
                            paddingTop = Padding.MEDIUM
                        }
                }
                GroupHarvestField.AUTHOR -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.authorInfo
                        ?.let { "${it.byName} ${it.lastName}" }
                        ?.createValueField(
                            fieldId = field,
                            label = RR.string.group_hunting_harvest_field_author
                        ) {
                            paddingTop = Padding.MEDIUM
                            paddingBottom = Padding.LARGE
                        }
                }
                GroupHarvestField.GENDER -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.gender?.value
                        ?.let { gender ->
                            GenderField(field, gender) {
                                readOnly = true
                            }
                        }
                }
                GroupHarvestField.AGE -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.age?.value
                        ?.let { age ->
                            AgeField(field, age) {
                                readOnly = true
                                paddingBottom = Padding.MEDIUM_LARGE
                            }
                        }
                }
                GroupHarvestField.NOT_EDIBLE -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.notEdible
                        ?.let {
                            when (it) {
                                true -> RR.string.generic_yes
                                false -> RR.string.generic_no
                            }
                        }
                        ?.let { stringProvider.getString(it) }
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_not_edible
                        )
                }
                GroupHarvestField.WEIGHT_ESTIMATED -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.weightEstimated
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_weight_estimated
                        )
                }
                GroupHarvestField.WEIGHT_MEASURED -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.weightMeasured
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_weight_measured
                        )
                }
                GroupHarvestField.FITNESS_CLASS -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.fitnessClass
                        ?.let {
                            it.value?.resourcesStringId?.let { stringId ->
                                stringProvider.getString(stringId)
                            } ?: it.rawBackendEnumValue
                        }
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_fitness_class
                        )
                }
                GroupHarvestField.ANTLERS_TYPE -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.antlersType
                        ?.let {
                            it.value?.resourcesStringId?.let { stringId ->
                                stringProvider.getString(stringId)
                            } ?: it.rawBackendEnumValue
                        }
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_antlers_type
                        )
                }
                GroupHarvestField.ANTLERS_WIDTH -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.antlersWidth
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_antlers_width
                        )
                }
                GroupHarvestField.ANTLER_POINTS_LEFT -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.antlerPointsLeft
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_antler_points_left
                        )
                }
                GroupHarvestField.ANTLER_POINTS_RIGHT -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.antlerPointsRight
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_antler_points_right
                        )
                }
                GroupHarvestField.ANTLERS_LOST -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.antlersLost
                        ?.let {
                            when (it) {
                                true -> RR.string.generic_yes
                                false -> RR.string.generic_no
                            }
                        }
                        ?.let { stringProvider.getString(it) }
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_antlers_lost
                        )
                }
                GroupHarvestField.ANTLERS_GIRTH -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.antlersGirth
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_antlers_girth
                        )
                }
                GroupHarvestField.ANTLER_SHAFT_WIDTH -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.antlerShaftWidth
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_antler_shaft_width
                        )
                }
                GroupHarvestField.ANTLERS_LENGTH -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.antlersLength
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_antlers_length
                        )
                }
                GroupHarvestField.ANTLERS_INNER_WIDTH -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.antlersInnerWidth
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_antlers_inner_width
                        )
                }
                GroupHarvestField.ALONE -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.alone
                        ?.let {
                            when (it) {
                                true -> RR.string.generic_yes
                                false -> RR.string.generic_no
                            }
                        }
                        ?.let { stringProvider.getString(it) }
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_alone
                        )
                }
                GroupHarvestField.ADDITIONAL_INFORMATION -> field to { harvest: GroupHuntingHarvestData ->
                    harvest.specimens
                        .firstOrNull()
                        ?.additionalInfo
                        ?.createValueField(
                                fieldId = field,
                                label = RR.string.group_hunting_harvest_field_additional_information
                        )
                }
                GroupHarvestField.ACTOR_HUNTER_NUMBER,
                GroupHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
                GroupHarvestField.ANTLER_INSTRUCTIONS,
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS,
                GroupHarvestField.HUNTING_DAY_AND_TIME,
                GroupHarvestField.ERROR_DATE_NOT_WITHIN_GROUP_PERMIT,
                GroupHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
                GroupHarvestField.HEADLINE_SHOOTER,
                GroupHarvestField.HEADLINE_SPECIMEN,
                GroupHarvestField.WEIGHT -> null
            }
        }.associateBy(
                keySelector = { it.first },
                valueTransform =  { DataFieldProducerProxy(it.second) }
        )
    }

    private fun Any.createValueField(
        fieldId: GroupHarvestField,
        label: RR.string,
        configureSettings: (StringField.DefaultStringFieldSettings.() -> Unit)? = null
    ): StringField<GroupHarvestField> {
        return StringField(fieldId, this.toString()) {
            readOnly = true
            singleLine = true
            this.label = stringProvider.getString(label)
            paddingTop = Padding.SMALL_MEDIUM
            paddingBottom = Padding.SMALL

            configureSettings?.let { configure ->
                this.configure()
            }
        }
    }

    companion object {
        private val logger by getLogger(ViewGroupHarvestController::class)
    }
}

