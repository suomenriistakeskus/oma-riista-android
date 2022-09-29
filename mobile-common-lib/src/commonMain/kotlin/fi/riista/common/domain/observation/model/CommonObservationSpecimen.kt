package fi.riista.common.domain.observation.model

import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.observation.dto.CommonObservationSpecimenDTO
import fi.riista.common.model.BackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class CommonObservationSpecimen(
    val remoteId: Long?,
    val revision: Int?,
    val gender: BackendEnum<Gender>,
    val age: BackendEnum<GameAge>,
    val stateOfHealth: BackendEnum<ObservationSpecimenState>,
    val marking: BackendEnum<ObservationSpecimenMarking>,
    val widthOfPaw: Double?,
    val lengthOfPaw: Double?,
)

internal fun CommonObservationSpecimen.toObservationSpecimenDTO() =
    CommonObservationSpecimenDTO(
        id = remoteId,
        rev = revision,
        gender = gender.rawBackendEnumValue,
        age = age.rawBackendEnumValue,
        state = stateOfHealth.rawBackendEnumValue,
        marking = marking.rawBackendEnumValue,
        lengthOfPaw = lengthOfPaw,
        widthOfPaw = widthOfPaw,
    )

internal fun CommonObservationSpecimen.toCommonSpecimenData() =
    CommonSpecimenData(
        remoteId = remoteId,
        revision = revision,
        gender = gender,
        age = age,
        stateOfHealth = stateOfHealth,
        marking = marking,
        lengthOfPaw = lengthOfPaw,
        widthOfPaw = widthOfPaw,
    )
