package fi.riista.common.domain.specimens.ui.edit

import co.touchlab.stately.concurrency.AtomicReference
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldId
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenSpecifications
import fi.riista.common.model.BackendEnum
import fi.riista.common.ui.dataField.DataFieldViewModel
import fi.riista.common.ui.dataField.DataFields

data class EditSpecimensViewModel internal constructor(
    internal val species: Species,
    internal val fieldSpecifications: List<SpecimenFieldSpecification>,
    // keep specimens in LinkedHashMap in order to have stable specimen indices
    // -> stable indices allows e.g. remove animations
    internal val specimens: AtomicReference<LinkedHashMap<Int, CommonSpecimenData>>,
    internal val allowedAges: List<BackendEnum<GameAge>>,
    internal val allowedStatesOfHealth: List<BackendEnum<ObservationSpecimenState>>,
    internal val allowedMarkings: List<BackendEnum<ObservationSpecimenMarking>>,
    internal val maxLengthOfPawCentimetres: Double?,
    internal val minLengthOfPawCentimetres: Double?,
    internal val maxWidthOfPawCentimetres: Double?,
    internal val minWidthOfPawCentimetres: Double?,
    override val fields: DataFields<SpecimenFieldId>,
) : DataFieldViewModel<SpecimenFieldId>() {

    internal constructor(
        specimenSpecifications: SpecimenSpecifications,
        specimens: LinkedHashMap<Int, CommonSpecimenData>
    ): this(
        species = specimenSpecifications.species,
        fieldSpecifications = specimenSpecifications.fieldSpecifications,
        specimens = AtomicReference(specimens),
        allowedAges = specimenSpecifications.allowedAges,
        allowedStatesOfHealth = specimenSpecifications.allowedStatesOfHealth,
        allowedMarkings = specimenSpecifications.allowedMarkings,
        maxLengthOfPawCentimetres = specimenSpecifications.maxLengthOfPawCentimetres,
        minLengthOfPawCentimetres = specimenSpecifications.minLengthOfPawCentimetres,
        maxWidthOfPawCentimetres = specimenSpecifications.maxWidthOfPawCentimetres,
        minWidthOfPawCentimetres = specimenSpecifications.minWidthOfPawCentimetres,
        fields = listOf(),
    )

    /**
     * Specimen data based on current viewmodel values.
     *
     * Intentionally not kept in a field as that could cause misunderstanding when accessing specimens:
     * - should specimens be obtained from [SpecimenFieldDataContainer.specimens] or
     * - should [specimens] be used
     */
    val specimenData: SpecimenFieldDataContainer
        get() {
            return SpecimenFieldDataContainer(
                species = species,
                specimens = specimens.get().values.toList(),
                fieldSpecifications = fieldSpecifications,
                allowedAges = allowedAges,
                allowedStatesOfHealth = allowedStatesOfHealth,
                allowedMarkings = allowedMarkings,
                maxLengthOfPawCentimetres = maxLengthOfPawCentimetres,
                minLengthOfPawCentimetres = minLengthOfPawCentimetres,
                maxWidthOfPawCentimetres = maxWidthOfPawCentimetres,
                minWidthOfPawCentimetres = minWidthOfPawCentimetres,
            )
        }
}
