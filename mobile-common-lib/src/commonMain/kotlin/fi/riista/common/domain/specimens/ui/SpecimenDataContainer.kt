package fi.riista.common.domain.specimens.ui

import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.model.BackendEnum
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.FieldSpecification
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.floor

abstract class SpecimenSpecifications {
    internal abstract val species: Species

    /**
     * The specifications for fields i.e. how each piece of data should be
     * displayed and/or entered.
     */
    internal abstract val fieldSpecifications: List<SpecimenFieldSpecification>
    internal abstract val allowedAges: List<BackendEnum<GameAge>>
    internal abstract val allowedStatesOfHealth: List<BackendEnum<ObservationSpecimenState>>
    internal abstract val allowedMarkings: List<BackendEnum<ObservationSpecimenMarking>>

    internal abstract val maxLengthOfPawCentimetres: Double?
    internal abstract val minLengthOfPawCentimetres: Double?
    internal abstract val maxWidthOfPawCentimetres: Double?
    internal abstract val minWidthOfPawCentimetres: Double?

    internal val containsOnlyAdultYoungOrUnknownAgeValues: Boolean by lazy {
        val ages = setOf(GameAge.ADULT, GameAge.YOUNG, GameAge.UNKNOWN).mapTo(HashSet()) { BackendEnum.create(it) }
        (allowedAges - ages).isEmpty()
    }

    internal val possibleWidthsOfPawInMillimeters: IntProgression? by lazy {
        calculatePossiblePawDimensionValuesInMillimeters(
            minDimensionCentimeters = minWidthOfPawCentimetres,
            maxDimensionCentimeters = maxWidthOfPawCentimetres,
        )
    }

    internal val possibleLengthsOfPawInMillimeters: IntProgression? by lazy {
        calculatePossiblePawDimensionValuesInMillimeters(
            minDimensionCentimeters = minLengthOfPawCentimetres,
            maxDimensionCentimeters = maxLengthOfPawCentimetres,
        )
    }

    private fun calculatePossiblePawDimensionValuesInMillimeters(
        minDimensionCentimeters: Double?,
        maxDimensionCentimeters: Double?,
    ): IntProgression? {
        if (minDimensionCentimeters == null || maxDimensionCentimeters == null) {
            return null
        }

        // figure out the limits rounded to 5mm accuracy
        // - split conversion in two phases: first multiply by 2 and then multiply by 5
        // -> round between to get 5mm accuracy in final result
        val minDimensionMillimeters = ceil(minDimensionCentimeters * 2).toInt() * 5
        val maxDimensionMillimeters = floor(maxDimensionCentimeters * 2).toInt() * 5

        return IntProgression.fromClosedRange(
            rangeStart = minDimensionMillimeters,
            rangeEnd = maxDimensionMillimeters,
            step = 5
        )
    }
}

@Serializable
data class SpecimenFieldDataContainer internal constructor(
    override val species: Species,
    /**
     * The amount of specimens to be displayed.
     */
    internal val specimenAmount: Int,

    /**
     * The raw specimen information. The list size may differ from [specimenAmount] (it may be larger or smaller)
     * and thus [specimenAmount] should be the key factor when deciding how many specimens are displayed.
     */
    internal val specimens: List<CommonSpecimenData>,

    /**
     * The specifications for fields i.e. how each piece of data should be
     * displayed and/or entered.
     */
    override val fieldSpecifications: List<SpecimenFieldSpecification>,

    override val allowedAges: List<BackendEnum<GameAge>>,
    override val allowedStatesOfHealth: List<BackendEnum<ObservationSpecimenState>>,
    override val allowedMarkings: List<BackendEnum<ObservationSpecimenMarking>>,

    override val maxLengthOfPawCentimetres: Double?,
    override val minLengthOfPawCentimetres: Double?,
    override val maxWidthOfPawCentimetres: Double?,
    override val minWidthOfPawCentimetres: Double?,
): SpecimenSpecifications() {

    companion object {
        internal fun createForSrva(
            species: Species,
            specimenAmount: Int,
            specimens: List<CommonSpecimenData>,
            fieldSpecifications: List<SpecimenFieldSpecification>,
        ) = SpecimenFieldDataContainer(
            species = species,
            specimenAmount = specimenAmount,
            specimens = specimens,
            fieldSpecifications = fieldSpecifications,
            allowedAges = listOf(GameAge.ADULT, GameAge.YOUNG, GameAge.UNKNOWN).map { BackendEnum.create(it) },
            allowedStatesOfHealth = listOf(),
            allowedMarkings = listOf(),
            maxLengthOfPawCentimetres = null,
            minLengthOfPawCentimetres = null,
            maxWidthOfPawCentimetres = null,
            minWidthOfPawCentimetres = null,
        )

        internal fun createForHarvest(
            species: Species,
            specimenAmount: Int,
            specimens: List<CommonSpecimenData>,
            fieldSpecifications: List<SpecimenFieldSpecification>,
        ) = SpecimenFieldDataContainer(
            species = species,
            specimenAmount = specimenAmount,
            specimens = specimens,
            fieldSpecifications = fieldSpecifications,
            allowedAges = listOf(GameAge.ADULT, GameAge.YOUNG).map { BackendEnum.create(it) },
            allowedStatesOfHealth = listOf(),
            allowedMarkings = listOf(),
            maxLengthOfPawCentimetres = null,
            minLengthOfPawCentimetres = null,
            maxWidthOfPawCentimetres = null,
            minWidthOfPawCentimetres = null,
        )
    }
}

/**
 * Data classes cannot be extended and thus [FieldSpecification] contents
 * is copied here as well.
 */
@Serializable
data class SpecimenFieldSpecification(
    val fieldType: SpecimenFieldType,
    val label: String?,
    val requirementStatus: FieldRequirement,
)
