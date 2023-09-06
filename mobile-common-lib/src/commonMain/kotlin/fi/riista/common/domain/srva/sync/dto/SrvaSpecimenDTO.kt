package fi.riista.common.domain.srva.sync.dto

import fi.riista.common.domain.srva.model.CommonSrvaSpecimen
import fi.riista.common.model.toBackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class SrvaSpecimenDTO(
    val gender: String? = null,
    val age: String? = null,
)

fun SrvaSpecimenDTO.toCommonSrvaSpecimen(): CommonSrvaSpecimen {
    return CommonSrvaSpecimen(
        gender = gender.toBackendEnum(),
        age = age.toBackendEnum()
    )
}

fun CommonSrvaSpecimen.toSrvaSpecimenDTO(): SrvaSpecimenDTO {
    return SrvaSpecimenDTO(
        gender = gender.rawBackendEnumValue,
        age = age.rawBackendEnumValue,
    )
}
