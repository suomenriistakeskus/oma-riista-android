package fi.riista.common.domain.observation.dto

import fi.riista.common.domain.dto.*
import fi.riista.common.domain.observation.model.CommonObservationSpecimen
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class CommonObservationSpecimenDTO(
    val id: Long?,
    val rev: Int?,
    val gender: GenderDTO? = null,
    val age: GameAgeDTO? = null,
    val state: ObservationSpecimenStateDTO? = null,
    val marking: ObservationSpecimenMarkingDTO? = null,
    val widthOfPaw: Double? = null,
    val lengthOfPaw: Double? = null,
)

internal fun CommonObservationSpecimenDTO.toObservationSpecimen() : CommonObservationSpecimen {
    return CommonObservationSpecimen(
        remoteId = id,
        revision = rev,
        gender = gender.toBackendEnum(),
        age = age.toBackendEnum(),
        stateOfHealth = state.toBackendEnum(),
        marking = marking.toBackendEnum(),
        widthOfPaw = widthOfPaw,
        lengthOfPaw = lengthOfPaw,
    )
}
