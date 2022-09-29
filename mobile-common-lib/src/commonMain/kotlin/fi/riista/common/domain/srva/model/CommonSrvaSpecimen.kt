package fi.riista.common.domain.srva.model

import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.model.BackendEnum
import kotlinx.serialization.Serializable

@Serializable
data class CommonSrvaSpecimen(
    val gender: BackendEnum<Gender>,
    val age: BackendEnum<GameAge>,
)

internal fun CommonSrvaSpecimen.toCommonSpecimenData(): CommonSpecimenData {
    return CommonSpecimenData(
        gender = gender,
        age = age,
    )
}