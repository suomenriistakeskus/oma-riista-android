package fi.riista.common.domain.groupHunting.ui.groupObservation.view

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.constants.isDeer
import fi.riista.common.domain.constants.isDeerOrMoose
import fi.riista.common.domain.groupHunting.GroupHuntingContext
import fi.riista.common.domain.groupHunting.GroupHuntingObservationOperationResponse
import fi.riista.common.domain.groupHunting.HuntingClubGroupDataPiece
import fi.riista.common.domain.groupHunting.model.*
import fi.riista.common.domain.groupHunting.ui.GroupObservationField
import fi.riista.common.domain.groupHunting.ui.ObservationActionResolver
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
 * A controller for viewing proposed [GroupHuntingObservation] information
 */
class ViewGroupObservationController(
    private val groupHuntingContext: GroupHuntingContext,
    private val observationTarget: GroupHuntingObservationTarget,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<ViewGroupObservationViewModel>() {

    private lateinit var dataFieldProducers: List<DataFieldProducer<GroupHuntingObservationData, GroupObservationField>>

    init {
        // should be accessed from UI thread only
        ensureNeverFrozen()

        initializeFieldProducers()
    }

    /**
     * Loads the [GroupHuntingObservation] and updates the [viewModelLoadStatus] accordingly.
     */
    suspend fun loadObservation() {
        val loadFlow = createLoadViewModelFlow(refresh = false)

        loadFlow.collect { viewModelLoadStatus ->
            updateViewModel(viewModelLoadStatus)
        }
    }

    suspend fun rejectObservation(): GroupHuntingObservationOperationResponse {
        val viewModel = viewModelLoadStatus.value.loadedViewModel
            ?: run {
                logger.w { "Failed to obtain loaded viewmodel for handling intent" }
                return GroupHuntingObservationOperationResponse.Error
            }

        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
            identifiesClubAndGroup = observationTarget,
            allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to obtain group context in order to reject an observation" }
            return GroupHuntingObservationOperationResponse.Error
        }

        val observation = viewModel.observationData.toGroupHuntingObservation()
            ?: kotlin.run {
                logger.w { "Failed to convert observation data to observation!" }
                return GroupHuntingObservationOperationResponse.Error
            }

        return groupContext.rejectObservation(observation)
    }

    override fun createLoadViewModelFlow(refresh: Boolean): Flow<ViewModelLoadStatus<ViewGroupObservationViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)


        val groupContext = groupHuntingContext.fetchHuntingGroupContext(
            identifiesClubAndGroup = observationTarget,
            allowCached = true
        ) ?: kotlin.run {
            logger.w { "Failed to load a context for the hunting group ${observationTarget.huntingGroupId}" }
            emit(ViewModelLoadStatus.LoadFailed)
            return@flow
        }

        val observation = groupContext.fetchObservation(
            identifiesObservation = observationTarget,
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

        if (observation != null && huntingGroupStatus != null) {
            val observationData = observation.toGroupHuntingObservationData(groupMembers)

            emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    observationData = observationData,
                    canApproveObservation = ObservationActionResolver.canApproveObservation(
                        status = huntingGroupStatus,
                        observation = observationData
                    ),
                    canEditObservation = ObservationActionResolver.canEditObservation(
                        status = huntingGroupStatus,
                        observation = observationData
                    ),
                    canRejectObservation = ObservationActionResolver.canRejectObservation(
                        status = huntingGroupStatus,
                        observation = observationData
                    ),
                    huntingGroupArea = groupContext.huntingAreaProvider.area,
                )
            ))
        } else {
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    private fun createViewModel(
        observationData: GroupHuntingObservationData,
        canApproveObservation: Boolean,
        canEditObservation: Boolean,
        canRejectObservation: Boolean,
        huntingGroupArea: HuntingGroupArea?,
    ): ViewGroupObservationViewModel {
        return ViewGroupObservationViewModel(
            observationData = observationData,
            fields = dataFieldProducers.mapNotNull { producer ->
                producer.produceDataField(observationData)
            },
            canApproveObservation = canApproveObservation,
            canEditObservation = canEditObservation,
            canRejectObservation = canRejectObservation,
            huntingGroupArea = huntingGroupArea,
        )
    }

    private fun initializeFieldProducers() {
        dataFieldProducers = listOf(
            { observation: GroupHuntingObservationData ->
                LocationField(GroupObservationField.LOCATION, observation.geoLocation.asKnownLocation()) {
                    readOnly = true
                }
            },
            { observation: GroupHuntingObservationData ->
                SpeciesField(GroupObservationField.SPECIES_CODE, observation.gameSpeciesCode) {
                    readOnly = true
                }
            },
            { observation: GroupHuntingObservationData ->
                DateAndTimeField(GroupObservationField.DATE_AND_TIME, observation.pointOfTime) {
                    readOnly = true
                }
            },
            { observation: GroupHuntingObservationData ->

                val observationType = if (observation.observationType.value != null) {
                    stringProvider.getString(observation.observationType.value!!.resourcesStringId)
                } else {
                    ""
                }
                StringField(GroupObservationField.OBSERVATION_TYPE, observationType) {
                    readOnly = true
                    singleLine = true
                    paddingTop = Padding.MEDIUM
                    paddingBottom = Padding.SMALL
                    label = stringProvider.getString(RR.string.group_hunting_observation_field_observation_type)
                }
            },
            { observation: GroupHuntingObservationData ->
                observation.actorInfo.personWithHunterNumber
                    ?.let { observer ->
                        val actorName = "${observer.byName} ${observer.lastName}"
                        StringField(GroupObservationField.ACTOR, actorName) {
                            readOnly = true
                            singleLine = true
                            paddingTop = Padding.MEDIUM
                            paddingBottom = Padding.SMALL
                            label = stringProvider.getString(RR.string.group_hunting_observation_field_actor)
                        }
                    }
            },
            { observation: GroupHuntingObservationData ->
                observation.authorInfo
                    ?.let { author ->
                        val authorName = "${author.byName} ${author.lastName}"
                        StringField(GroupObservationField.AUTHOR, authorName) {
                            readOnly = true
                            singleLine = true
                            paddingTop = Padding.SMALL
                            paddingBottom = Padding.MEDIUM
                            label = stringProvider.getString(RR.string.group_hunting_observation_field_author)
                        }
                    }
            },
            { observation: GroupHuntingObservationData ->
                if (observation.gameSpeciesCode.isDeerOrMoose()) {
                    LabelField(
                        GroupObservationField.HEADLINE_SPECIMEN_DETAILS,
                        stringProvider.getString(RR.string.group_hunting_observation_field_headline_specimen_details),
                        LabelField.Type.CAPTION
                    ) {
                        paddingBottom = Padding.SMALL
                    }
                } else {
                    null
                }
            },
            { observation: GroupHuntingObservationData ->
                if (observation.gameSpeciesCode.isDeerOrMoose()) {
                    val amount = observation.mooselikeMaleAmount ?: 0
                    StringField(GroupObservationField.MOOSELIKE_MALE_AMOUNT, amount.toString()) {
                        readOnly = true
                        singleLine = true
                        paddingTop = Padding.SMALL
                        paddingBottom = Padding.SMALL
                        label = stringProvider.getString(
                            if (observation.gameSpeciesCode.isDeer()) {
                                RR.string.group_hunting_observation_field_mooselike_male_amount_within_deer_hunting
                            } else {
                                RR.string.group_hunting_observation_field_mooselike_male_amount
                            }
                        )
                    }
                } else {
                    null
                }
            },
            { observation: GroupHuntingObservationData ->
                if (observation.gameSpeciesCode.isDeerOrMoose()) {
                    val amount = observation.mooselikeFemaleAmount ?: 0
                    StringField(GroupObservationField.MOOSELIKE_FEMALE_AMOUNT, amount.toString()) {
                        readOnly = true
                        singleLine = true
                        paddingTop = Padding.SMALL
                        paddingBottom = Padding.SMALL
                        label = stringProvider.getString(
                            if (observation.gameSpeciesCode.isDeer()) {
                                RR.string.group_hunting_observation_field_mooselike_female_amount_within_deer_hunting
                            } else {
                                RR.string.group_hunting_observation_field_mooselike_female_amount
                            }
                        )
                    }
                } else {
                    null
                }
            },
            { observation: GroupHuntingObservationData ->
                if (observation.gameSpeciesCode.isDeerOrMoose()) {
                    val amount = observation.mooselikeFemale1CalfAmount ?: 0
                    StringField(GroupObservationField.MOOSELIKE_FEMALE_1CALF_AMOUNT, amount.toString()) {
                        readOnly = true
                        singleLine = true
                        paddingTop = Padding.SMALL
                        paddingBottom = Padding.SMALL
                        label = stringProvider.getString(
                            if (observation.gameSpeciesCode.isDeer()) {
                                RR.string.group_hunting_observation_field_mooselike_female_1calf_amount_within_deer_hunting
                            } else {
                                RR.string.group_hunting_observation_field_mooselike_female_1calf_amount
                            }
                        )
                    }
                } else {
                    null
                }
            },
            { observation: GroupHuntingObservationData ->
                if (observation.gameSpeciesCode.isDeerOrMoose()) {
                    val amount = observation.mooselikeFemale2CalfsAmount ?: 0
                    StringField(GroupObservationField.MOOSELIKE_FEMALE_2CALF_AMOUNT, amount.toString()) {
                        readOnly = true
                        singleLine = true
                        paddingTop = Padding.SMALL
                        paddingBottom = Padding.SMALL
                        label = stringProvider.getString(
                            if (observation.gameSpeciesCode.isDeer()) {
                                RR.string.group_hunting_observation_field_mooselike_female_2calf_amount_within_deer_hunting
                            } else {
                                RR.string.group_hunting_observation_field_mooselike_female_2calf_amount
                            }
                        )
                    }
                } else {
                    null
                }
            },
            { observation: GroupHuntingObservationData ->
                if (observation.gameSpeciesCode.isDeerOrMoose()) {
                    val amount = observation.mooselikeFemale3CalfsAmount ?: 0
                    StringField(GroupObservationField.MOOSELIKE_FEMALE_3CALF_AMOUNT, amount.toString()) {
                        readOnly = true
                        singleLine = true
                        paddingTop = Padding.SMALL
                        paddingBottom = Padding.SMALL
                        label = stringProvider.getString(
                            if (observation.gameSpeciesCode.isDeer()) {
                                RR.string.group_hunting_observation_field_mooselike_female_3calf_amount_within_deer_hunting
                            } else {
                                RR.string.group_hunting_observation_field_mooselike_female_3calf_amount
                            }
                        )
                    }
                } else {
                    null
                }
            },
            { observation: GroupHuntingObservationData ->
                if (observation.gameSpeciesCode == SpeciesCodes.WHITE_TAILED_DEER_ID) {
                    val amount = observation.mooselikeFemale4CalfsAmount ?: 0
                    StringField(GroupObservationField.MOOSELIKE_FEMALE_4CALF_AMOUNT, amount.toString()) {
                        readOnly = true
                        singleLine = true
                        paddingTop = Padding.SMALL
                        paddingBottom = Padding.SMALL
                        label = stringProvider.getString(
                            if (observation.gameSpeciesCode.isDeer()) {
                                RR.string.group_hunting_observation_field_mooselike_female_4calf_amount_within_deer_hunting
                            } else {
                                RR.string.group_hunting_observation_field_mooselike_female_4calf_amount
                            }
                        )
                    }
                } else {
                    null
                }
            },
            { observation: GroupHuntingObservationData ->
                if (observation.gameSpeciesCode.isDeerOrMoose()) {
                    val amount = observation.mooselikeCalfAmount ?: 0
                    StringField(GroupObservationField.MOOSELIKE_CALF_AMOUNT, amount.toString()) {
                        readOnly = true
                        singleLine = true
                        paddingTop = Padding.SMALL
                        paddingBottom = Padding.SMALL
                        label = stringProvider.getString(
                            if (observation.gameSpeciesCode.isDeer()) {
                                RR.string.group_hunting_observation_field_mooselike_calf_amount_within_deer_hunting
                            } else {
                                RR.string.group_hunting_observation_field_mooselike_calf_amount
                            }
                        )
                    }
                } else {
                    null
                }
            },
            { observation: GroupHuntingObservationData ->
                if (observation.gameSpeciesCode.isDeerOrMoose()) {
                    val amount = observation.mooselikeUnknownSpecimenAmount ?: 0
                    StringField(GroupObservationField.MOOSELIKE_UNKNOWN_SPECIMEN_AMOUNT, amount.toString()) {
                        readOnly = true
                        singleLine = true
                        paddingTop = Padding.SMALL
                        paddingBottom = Padding.LARGE
                        label = stringProvider.getString(
                            if (observation.gameSpeciesCode.isDeer()) {
                                RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount_within_deer_hunting
                            } else {
                                RR.string.group_hunting_observation_field_mooselike_unknown_specimen_amount
                            }
                        )
                    }
                } else {
                    null
                }
            },
        ).map { DataFieldProducerProxy(it) }
    }

    companion object {
        private val logger by getLogger(ViewGroupObservationController::class)
    }
}
