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
    val remoteId: Long? = null,
    val revision: Int? = null,
    val gender: BackendEnum<Gender>? = null,
    val age: BackendEnum<GameAge>? = null,
    val stateOfHealth: BackendEnum<ObservationSpecimenState>? = null,
    val marking: BackendEnum<ObservationSpecimenMarking>? = null,
    val lengthOfPaw: Double? = null,
    val widthOfPaw: Double? = null,
)
