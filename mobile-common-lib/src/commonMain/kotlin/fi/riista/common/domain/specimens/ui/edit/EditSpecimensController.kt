package fi.riista.common.domain.specimens.ui.edit

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.ensureNeverFrozen
import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.logging.getLogger
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.controller.ControllerWithLoadableModel
import fi.riista.common.ui.controller.ViewModelLoadStatus
import fi.riista.common.ui.dataField.DataField
import fi.riista.common.ui.intent.IntentHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow


/**
 * A controller for editing [SpecimenFieldDataContainer] information
 */
class EditSpecimensController(
    private val speciesResolver: SpeciesResolver,
    private val stringProvider: StringProvider,
) : ControllerWithLoadableModel<EditSpecimensViewModel>(),
    IntentHandler<EditSpecimenIntent> {

    private var specimenData: SpecimenFieldDataContainer? = null

    val eventDispatchers: EditSpecimenEventDispatcher by lazy {
        EditSpecimenEventToIntentMapper(intentHandler = this)
    }

    private var _specimenFieldProducer: EditSpecimensFieldProducer =
        EditSpecimensFieldProducer(
            species = specimenData?.species ?: Species.Unknown,
            speciesResolver = speciesResolver,
            stringProvider = stringProvider,
        )

    private val specimenFieldProducer: EditSpecimensFieldProducer
        get() {
            val species = specimenData?.species ?: Species.Unknown
            if (_specimenFieldProducer.species != species) {
                _specimenFieldProducer = EditSpecimensFieldProducer(
                    species = species,
                    speciesResolver = speciesResolver,
                    stringProvider = stringProvider,
                )
            }

            return _specimenFieldProducer
        }

    init {
        // should be accessed from UI thread only
        ensureNeverFrozen()
    }

    fun addSpecimen() {
        val viewModel = getLoadedViewModelOrNull()
        if (viewModel == null) {
            logger.w { "No loaded viewmodel, cannot remove specimen" }
            return
        }

        val largestIndex = viewModel.specimens.get().keys.fold(initial = -1) { currentMaxIndex, index ->
            maxOf(currentMaxIndex, index)
        }

        val specimens = viewModel.editableSpecimens()
        specimens[largestIndex + 1] = CommonSpecimenData()
        viewModel.specimens.set(specimens)

        updateViewModel(ViewModelLoadStatus.Loaded(
            viewModel = viewModel.withUpdatedFields()
        ))
    }

    fun removeSpecimen(fieldId: SpecimenFieldId) {
        if (fieldId.type != SpecimenFieldType.SPECIMEN_HEADER) {
            logger.d { "Refusing to remove based on other fields than header" }
            return
        }

        val viewModel = getLoadedViewModelOrNull()
        if (viewModel == null) {
            logger.w { "No loaded viewmodel, cannot remove specimen" }
            return
        }

        val specimens = viewModel.editableSpecimens()
        specimens.remove(key = fieldId.index)
        viewModel.specimens.set(specimens)

        updateViewModel(ViewModelLoadStatus.Loaded(
            viewModel = viewModel.withUpdatedFields()
        ))
    }

    override fun handleIntent(intent: EditSpecimenIntent) {
        val viewModel = getLoadedViewModelOrNull()
        if (viewModel != null) {
            updateViewModel(ViewModelLoadStatus.Loaded(
                viewModel = handleIntent(intent, viewModel)
            ))
        } else {
            logger.w { "No loaded viewmodel, cannot handle intent" }
        }
    }

    private fun handleIntent(
        intent: EditSpecimenIntent,
        viewModel: EditSpecimensViewModel,
    ): EditSpecimensViewModel {
        val specimenIndex = intent.fieldId.index
        val originalSpecimen = viewModel.specimens.get()[specimenIndex]
            ?: kotlin.run {
                logger.w { "No specimen for index $specimenIndex, cannot handle $intent!" }
                return viewModel
            }

        val updatedSpecimen = when (intent) {
            is EditSpecimenIntent.ChangeAge ->
                originalSpecimen.copy(age = intent.age)
            is EditSpecimenIntent.ChangeGender ->
                originalSpecimen.copy(gender = intent.gender?.toBackendEnum())
            is EditSpecimenIntent.ChangeSpecimenMarking ->
                originalSpecimen.copy(marking = intent.marking)
            is EditSpecimenIntent.ChangeStateOfHealth ->
                originalSpecimen.copy(stateOfHealth = intent.stateOfHealth)
            is EditSpecimenIntent.ChangeWidthOfPaw ->
                originalSpecimen.copy(widthOfPaw = intent.widthOfPawMillimeters?.div(10.0))
            is EditSpecimenIntent.ChangeLengthOfPaw ->
                originalSpecimen.copy(lengthOfPaw = intent.lengthOfPawMillimeters?.div(10.0))
        }

        val specimens = viewModel.editableSpecimens()
        specimens[specimenIndex] = updatedSpecimen
        viewModel.specimens.set(specimens)

        return viewModel.withUpdatedFields()
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
            Flow<ViewModelLoadStatus<EditSpecimensViewModel>> = flow {
        emit(ViewModelLoadStatus.Loading)

        val specimenData = specimenData
        if (specimenData != null) {
            val specimens = LinkedHashMap<Int, CommonSpecimenData>()
            specimenData.specimens.forEachIndexed { index, specimen ->
                specimens[index] = specimen
            }

            emit(ViewModelLoadStatus.Loaded(
                viewModel = EditSpecimensViewModel(
                    specimenSpecifications = specimenData,
                    specimens = specimens,
                ).withUpdatedFields()
            ))
        } else {
            logger.w { "Did you forget to set SpecimenFieldDataContainer event before loading viewModel?" }
            emit(ViewModelLoadStatus.LoadFailed)
        }
    }

    private fun EditSpecimensViewModel.withUpdatedFields(): EditSpecimensViewModel {
        return copy(
            fields = produceDataFields(this),
        )
    }

    private fun produceDataFields(viewModel: EditSpecimensViewModel): List<DataField<SpecimenFieldId>> {
        val specimenCount = viewModel.specimens.get().size
        return viewModel.specimens.get().entries.flatMapIndexed { runningSpecimenIndex, entry ->
            // stable index of the specimen - remains the same even if specimens removed / added
            val stableSpecimenIndex = entry.key
            val specimen = entry.value

            listOf(
                specimenFieldProducer.createSpeciesHeader(
                    stableSpecimenIndex = stableSpecimenIndex,
                    displayedSpecimenIndex = runningSpecimenIndex,
                    specimenCount = specimenCount,
                )
            ) +
            viewModel.fieldSpecifications.mapNotNull { fieldSpecification ->
                specimenFieldProducer.createField(
                    fieldSpecification = fieldSpecification,
                    specimen = specimen,
                    stableSpecimenIndex = stableSpecimenIndex,
                    specimenSpecifications = viewModel.specimenData
                )
            }
        }
    }



    companion object {
        private val logger by getLogger(EditSpecimensController::class)
    }
}

private fun EditSpecimensViewModel.editableSpecimens(): LinkedHashMap<Int, CommonSpecimenData> {
    val editableSpecimens = LinkedHashMap<Int, CommonSpecimenData>()
    editableSpecimens.putAll(specimens.get())
    return editableSpecimens
}
