package fi.riista.common.model

import fi.riista.common.dto.ObservationSpecimenDTO
import kotlinx.serialization.Serializable

typealias ObservationSpecimenId = Long

@Serializable
data class ObservationSpecimen(
    val id: ObservationSpecimenId,
    val rev: Int,
    val gender: BackendEnum<Gender>? = null,
    val age: BackendEnum<ObservedGameAge>? = null,
    val state: BackendEnum<ObservedGameState>? = null,
    val marking: BackendEnum<GameMarking>? = null,
    val widthOfPaw: Double? = null,
    val lengthOfPaw: Double? = null,
)

internal fun ObservationSpecimen.toObservationSpecimenDTO(): ObservationSpecimenDTO {
    return ObservationSpecimenDTO(
        id = id,
        rev = rev,
        gender = gender?.rawBackendEnumValue,
        age = age?.rawBackendEnumValue,
        state = state?.rawBackendEnumValue,
        marking = marking?.rawBackendEnumValue,
        widthOfPaw = widthOfPaw,
        lengthOfPaw = lengthOfPaw
    )
}
