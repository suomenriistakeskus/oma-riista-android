package fi.riista.common.dto

import fi.riista.common.model.*
import kotlinx.serialization.Serializable

@Serializable
data class ObservationSpecimenDTO(
    val id: Long,
    val rev: Int,
    val gender: GenderDTO? = null,
    val age: ObservedGameAgeDTO? = null,
    val state: ObservedGameStateDTO? = null,
    val marking: GameMarkingDTO? = null,
    val widthOfPaw: Double? = null,
    val lengthOfPaw: Double? = null,
)

internal fun ObservationSpecimenDTO.toObservationSpecimen() : ObservationSpecimen {
    return ObservationSpecimen(
            id = id,
            rev = rev,
            gender = gender.toBackendEnum(),
            age = age.toBackendEnum(),
            state = state.toBackendEnum(),
            marking = marking.toBackendEnum(),
            widthOfPaw = widthOfPaw,
            lengthOfPaw = lengthOfPaw,
    )
}
