package fi.riista.common.domain.model

import fi.riista.common.domain.observation.model.ObservationSpecimenMarking
import fi.riista.common.domain.observation.model.ObservationSpecimenState
import fi.riista.common.model.BackendEnum
import kotlinx.serialization.Serializable

/**
 * A specimen data wrapper that may contain specimen data for harvests, observations,
 * srvas etc. Probably contains extra fields for certain needs (e.g. srva only uses
 * few fields where observation may have more observation related data).
 *
 * The main purpose of this class is to be able to pass specimen data to a separate view+controller
 * in order to view and/or modify specimens. Naturally we would need to pass also information
 * what fields are expected as well as labels / requirement status for those fields.
 */
@Serializable
internal data class CommonSpecimenData(
    val remoteId: Long?,
    val revision: Int?,
    val gender: BackendEnum<Gender>?,
    val age: BackendEnum<GameAge>?,

    // mostly observation
    val stateOfHealth: BackendEnum<ObservationSpecimenState>?,
    val marking: BackendEnum<ObservationSpecimenMarking>?,
    val lengthOfPaw: Double?,
    val widthOfPaw: Double?,

    // mostly harvest
    val weight: Double?,
    val weightEstimated: Double?,
    val weightMeasured: Double?,
    val fitnessClass: BackendEnum<GameFitnessClass>?,
    val antlersLost: Boolean?,
    val antlersType: BackendEnum<GameAntlersType>?,
    val antlersWidth: Int?,
    val antlerPointsLeft: Int?,
    val antlerPointsRight: Int?,
    val antlersGirth: Int?,
    val antlersLength: Int?,
    val antlersInnerWidth: Int?,
    val antlerShaftWidth: Int?,
    val notEdible: Boolean?,
    val alone: Boolean?,
    val additionalInfo: String?
) {
    constructor(): this(
        remoteId = null,
        revision = null,
        gender = null,
        age = null,
        stateOfHealth = null,
        marking = null,
        lengthOfPaw = null,
        widthOfPaw = null,
        weight = null,
        weightEstimated = null,
        weightMeasured = null,
        fitnessClass = null,
        antlersLost = null,
        antlersType = null,
        antlersWidth = null,
        antlerPointsLeft = null,
        antlerPointsRight = null,
        antlersGirth = null,
        antlersLength = null,
        antlersInnerWidth = null,
        antlerShaftWidth = null,
        notEdible = null,
        alone = null,
        additionalInfo = null,
    )

    fun isEmpty(): Boolean {
        return this == EMPTY_SPECIMEN
    }

    companion object {
        // add so that this can be extended
    }
}

private val EMPTY_SPECIMEN = CommonSpecimenData()

internal fun Iterable<CommonSpecimenData>.keepNonEmpty() = filter { !it.isEmpty() }
