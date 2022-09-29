package fi.riista.common.domain.specimens.ui.view

import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.model.localizationKey
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.logging.getLogger
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.dataField.LabelField
import fi.riista.common.ui.dataField.Padding
import fi.riista.common.ui.dataField.StringField
import fi.riista.common.util.toStringOrMissingIndicator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


/**
 * A controller for viewing [SpecimenFieldDataContainer] information
 */
class ViewSpecimensController(
    private val speciesResolver: SpeciesResolver,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<ViewSpecimensViewModel>() {

    private var specimenData: SpecimenFieldDataContainer? = null

    init {
        // should be accessed from UI thread only
        ensureNeverFrozen()
    }

    /**
     * Loads the [SpecimenFieldDataContainer] and updates the [viewModelLoadStatus] accordingly.
     */
    suspend fun loadSpecimenData(specimenData: SpecimenFieldDataContainer) {
        this.specimenData = specimenData
        val loadFlow = createLoadViewModelFlow(refresh = false)

        loadFlow.collect { viewModelLoadStatus ->
            updateViewModel(viewModelLoadStatus)
        }
    }

    override fun createLoadViewModelFlow(refresh: Boolean):
            Flow<ViewModelLoadStatus<ViewSpecimensViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val specimenData = specimenData
        if (specimenData != null) {
            emit(ViewModelLoadStatus.Loaded(
                viewModel = createViewModel(
                    specimenData = specimenData,
                )
            ))
        } else {
            logger.w { "Did you forget to set SpecimenFieldDataContainer event before loading viewModel?" }
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    private fun createViewModel(
        specimenData: SpecimenFieldDataContainer,
    ): ViewSpecimensViewModel {
        return ViewSpecimensViewModel(
            specimenData = specimenData,
            fields = produceDataFields(specimenData),
        )
    }

    private fun produceDataFields(specimenData: SpecimenFieldDataContainer): List<DataField<SpecimenFieldId>> {
        return specimenData.specimens.flatMapIndexed { index: Int, specimen: CommonSpecimenData ->
            listOf<DataField<SpecimenFieldId>>(
                createSpeciesHeader(species = specimenData.species, index = index)
            ) +
            specimenData.fieldSpecifications.mapNotNull { fieldSpecification ->
                when (fieldSpecification.fieldType) {
                    SpecimenFieldType.GENDER ->
                        specimen.gender?.localized(stringProvider)
                            .createValueField(
                                fieldId = fieldSpecification.fieldType.toField(index),
                                fieldSpecification = fieldSpecification
                            )
                    SpecimenFieldType.AGE ->
                        specimen.age
                            ?.let { age ->
                                age.value?.let {
                                    stringProvider.getString(it.localizationKey(species = specimenData.species))
                                } ?: age.rawBackendEnumValue
                            }
                            .createValueField(
                                fieldId = fieldSpecification.fieldType.toField(index),
                                fieldSpecification = fieldSpecification
                            )
                    SpecimenFieldType.WIDTH_OF_PAW ->
                        specimen.widthOfPaw
                            .createValueField(
                                fieldId = fieldSpecification.fieldType.toField(index),
                                fieldSpecification = fieldSpecification
                            )
                    SpecimenFieldType.LENGTH_OF_PAW ->
                        specimen.lengthOfPaw
                            .createValueField(
                                fieldId = fieldSpecification.fieldType.toField(index),
                                fieldSpecification = fieldSpecification
                            )
                    SpecimenFieldType.STATE_OF_HEALTH ->
                        specimen.stateOfHealth?.localized(stringProvider)
                            .createValueField(
                                fieldId = fieldSpecification.fieldType.toField(index),
                                fieldSpecification = fieldSpecification
                            )
                    SpecimenFieldType.MARKING ->
                        specimen.marking?.localized(stringProvider)
                            .createValueField(
                                fieldId = fieldSpecification.fieldType.toField(index),
                                fieldSpecification = fieldSpecification
                            )
                    SpecimenFieldType.SPECIMEN_HEADER -> null // header added separately
                }
            }
        }
    }

    private fun createSpeciesHeader(species: Species, index: Int): LabelField<SpecimenFieldId> {
        val titleText = when (species) {
            is Species.Known ->
                speciesResolver.getSpeciesName(speciesCode = species.speciesCode)
                    ?: stringProvider.getString(RR.string.other_species)
            Species.Other ->
                stringProvider.getString(RR.string.other_species)
            Species.Unknown ->
                stringProvider.getString(RR.string.unknown_species)
        }

        return LabelField(
            id = SpecimenFieldType.SPECIMEN_HEADER.toField(index),
            text = "$titleText ${index + 1}",
            type = LabelField.Type.CAPTION
        ) {
            paddingTop = if (index > 0) {
                Padding.LARGE
            } else {
                Padding.MEDIUM
            }
            paddingBottom = Padding.NONE
        }
    }

    private fun Any?.createValueField(
        fieldId: SpecimenFieldId,
        fieldSpecification: SpecimenFieldSpecification,
        configureSettings: (StringField.DefaultStringFieldSettings.() -> Unit)? = null
    ): StringField<SpecimenFieldId> {
        val value = this.toStringOrMissingIndicator()

        return StringField(fieldId, value) {
            readOnly = true
            singleLine = true
            this.label = fieldSpecification.label
            paddingTop = Padding.SMALL_MEDIUM
            paddingBottom = Padding.SMALL

            configureSettings?.let { configure ->
                this.configure()
            }
        }
    }

    companion object {
        private val logger by getLogger(ViewSpecimensController::class)
    }
}

